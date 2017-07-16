package org.sbfc.converter.sbgnml2sbml;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.sbfc.converter.sbgnml2sbml.qual.SWrapperQualitativeSpecies;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.render.ColorDefinition;
import org.sbml.jsbml.ext.render.Ellipse;
import org.sbml.jsbml.ext.render.FontFamily;
import org.sbml.jsbml.ext.render.GraphicalPrimitive2D.FillRule;
import org.sbml.jsbml.ext.render.HTextAnchor;
import org.sbml.jsbml.ext.render.Image;
import org.sbml.jsbml.ext.render.LineEnding;
import org.sbml.jsbml.ext.render.ListOfLocalRenderInformation;
import org.sbml.jsbml.ext.render.LocalRenderInformation;
import org.sbml.jsbml.ext.render.LocalStyle;
import org.sbml.jsbml.ext.render.Rectangle;
import org.sbml.jsbml.ext.render.RenderConstants;
import org.sbml.jsbml.ext.render.RenderGraphicalObjectPlugin;
import org.sbml.jsbml.ext.render.RenderGroup;
import org.sbml.jsbml.ext.render.RenderLayoutPlugin;
import org.sbml.jsbml.ext.render.Text;
import org.sbml.jsbml.ext.render.VTextAnchor;

public class SBGNML2SBMLRender {
	SWrapperModel sWrapperModel;
	SBGNML2SBMLOutput sOutput;
	
	public SBGNML2SBMLRender(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput) {
		this.sWrapperModel = sWrapperModel;
		this.sOutput = sOutput;
		//createColourDefinitions();
		
		//createLineEndings();
	}
	
	public void renderGeneralGlyphs() {
		//todo: generalGlyphs as species (for a logic operator)?
		// as reactions might not work
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		for (String key : sWrapperModel.listOfWrapperGeneralGlyphs.keySet()){
			sWrapperGeneralGlyph = sWrapperModel.getWrapperGeneralGlyph(key);
			createStyle(sWrapperGeneralGlyph.generalGlyph, sWrapperGeneralGlyph.clazz);
			renderReferenceGlyphs(sWrapperGeneralGlyph);
		}
	}	
	
	public void renderCompartmentGlyphs() {
		SWrapperCompartmentGlyph sWrapperCompartmentGlyph;
		
		sWrapperModel.sortCompartmentOrderList();
		for (String key : sWrapperModel.compartmentOrderList.keySet()){
			sWrapperCompartmentGlyph = sWrapperModel.getWrapperCompartmentGlyph(key);
			createStyle(sWrapperCompartmentGlyph.compartmentGlyph, sWrapperCompartmentGlyph.clazz);
			
			//System.out.println("renderCompartmentGlyphs id="+sWrapperCompartmentGlyph.glyph.getId());
		}			
	}
	
	public void renderSpeciesGlyphs() {
		SWrapperSpeciesGlyph sWrapperSpeciesGlyph;
		for (String key : sWrapperModel.listOfWrapperSpeciesGlyphs.keySet()){
			sWrapperSpeciesGlyph = sWrapperModel.getWrapperSpeciesGlyph(key);
			createStyle(sWrapperSpeciesGlyph.speciesGlyph, sWrapperSpeciesGlyph.clazz);
		}		
		
		SWrapperQualitativeSpecies sWrapperQualitativeSpecies;
		for (String key : sWrapperModel.listOfSWrapperQualitativeSpecies.keySet()){
			sWrapperQualitativeSpecies = sWrapperModel.getSWrapperQualitativeSpecies(key);
			createStyle(sWrapperQualitativeSpecies.speciesGlyph, sWrapperQualitativeSpecies.clazz);
		}	
	}
	
	public void renderReactionGlyphs() {
		SWrapperReactionGlyph sWrapperReactionGlyph;
		for (String key : sWrapperModel.listOfWrapperReactionGlyphs.keySet()){
			sWrapperReactionGlyph = sWrapperModel.getWrapperReactionGlyph(key);
			createStyle(sWrapperReactionGlyph.reactionGlyph, sWrapperReactionGlyph.clazz);
			renderSpeciesReferenceGlyphs(sWrapperReactionGlyph);
		}		
	}
	
	public void renderSpeciesReferenceGlyphs(SWrapperReactionGlyph sWrapperReactionGlyph) {
		Arc arc;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		System.out.println("===renderSpeciesReferenceGlyphs "+sWrapperReactionGlyph.speciesReferenceGlyphs.keySet().size());
		
		for (String arcKey : sWrapperReactionGlyph.speciesReferenceGlyphs.keySet()){
			speciesReferenceGlyph = sWrapperReactionGlyph.speciesReferenceGlyphs.get(arcKey);
			arc = sWrapperReactionGlyph.getArc(arcKey);
			//System.out.println("===renderSpeciesReferenceGlyphs "+sWrapperReactionGlyph.consumptionArcs.size());
			//System.out.println("===renderSpeciesReferenceGlyphs "+sWrapperReactionGlyph.productionArcs.size());
			//System.out.println("===renderSpeciesReferenceGlyphs "+sWrapperReactionGlyph.modifierArcs.size());

			createStyle((GraphicalObject) speciesReferenceGlyph, arc);
		}		
	}
	
	public void renderReferenceGlyphs(SWrapperGeneralGlyph sWrapperGeneralGlyph) {
		Arc arc;
		ReferenceGlyph referenceGlyph;
		for (String arcKey : sWrapperGeneralGlyph.referenceGlyphs.keySet()){
			referenceGlyph = sWrapperGeneralGlyph.referenceGlyphs.get(arcKey);
			arc = sWrapperGeneralGlyph.arcs.get(arcKey);
			
			createStyle((GraphicalObject) referenceGlyph, arc);
		}		
	}
	
	public void createStyle(GraphicalObject graphicalObject, String clazz) {
		RenderGroup renderGroup;
		LocalStyle localStyle;
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin;
		LocalRenderInformation localRenderInformation = sOutput.localRenderInformation;
		Layout layout = sOutput.layout;
		
		String styleId = "LocalStyle_" + graphicalObject.getId();
		renderGroup = new RenderGroup(layout.getLevel(), layout.getVersion());
		initializeDefaultRenderGroup(renderGroup);
		localStyle = new LocalStyle(styleId, layout.getLevel(), layout.getVersion(), renderGroup);
		localRenderInformation.addLocalStyle(localStyle);
		localStyle.getRoleList().add(styleId);
		
		renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) graphicalObject.getPlugin(RenderConstants.shortLabel);
		renderGraphicalObjectPlugin.setObjectRole(styleId);		
				
		Image image = createImage(graphicalObject, clazz);
		
		renderGroup.addElement(image);		
	}
	
	public void createStyle(GraphicalObject graphicalObject, Arc arc) {
		RenderGroup renderGroup;
		LocalStyle localStyle;
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin;
		LocalRenderInformation localRenderInformation = sOutput.localRenderInformation;
		Layout layout = sOutput.layout;
		
		String styleId = "LocalStyle_" + graphicalObject.getId();
		renderGroup = new RenderGroup(layout.getLevel(), layout.getVersion());
		initializeDefaultRenderGroup(renderGroup);
		localStyle = new LocalStyle(styleId, layout.getLevel(), layout.getVersion(), renderGroup);
		localRenderInformation.addLocalStyle(localStyle);
		localStyle.getRoleList().add(styleId);
		
		renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) graphicalObject.getPlugin(RenderConstants.shortLabel);
		renderGraphicalObjectPlugin.setObjectRole(styleId);		
		
		if (arc.getClazz().equals("catalysis")){
			renderGroup.setEndHead("catalysisHead");			
		} else if (arc.getClazz().equals("production")){
			//LineEnding lineEnding = sOutput.listOfLineEndings.get("productionHead");
			renderGroup.setEndHead("productionHead");			
		} else if (arc.getClazz().equals("necessary stimulation")){
			//LineEnding lineEnding = sOutput.listOfLineEndings.get("productionHead");
			renderGroup.setEndHead("necessaryStimulationHead");			
		} else if (arc.getClazz().equals("unknown influence")){
			renderGroup.setEndHead("unknownModulationHead");	
		}

	}	
	
	public void createColourDefinitions() {
		LocalRenderInformation localRenderInformation = sOutput.localRenderInformation;
		Layout layout = sOutput.layout;
		
		ColorDefinition colorDefinition;
		colorDefinition = new ColorDefinition(layout.getLevel(), layout.getVersion());
		colorDefinition.setId("white");
		colorDefinition.setValue(Color.decode("#FFFFFF"));		
		localRenderInformation.addColorDefinition(colorDefinition);		
		
		colorDefinition = new ColorDefinition(layout.getLevel(), layout.getVersion());
		colorDefinition.setId("black");
		colorDefinition.setValue(Color.decode("#000000"));		
		localRenderInformation.addColorDefinition(colorDefinition);		
	}
	
	public void createLineEndings(ListOf<LineEnding>  listOfLineEndings) {
		sOutput.addListOfLineEndings(listOfLineEndings);
	}
	
	public void initializeDefaultRenderGroup(RenderGroup renderGroup) {
		renderGroup.setStroke("black");
		renderGroup.setStrokeWidth(3);
		renderGroup.setFillRule(FillRule.NONZERO);
		renderGroup.setFontSize((short) 12);
		renderGroup.setFontFamily(FontFamily.SANS_SERIF);
		renderGroup.setFontStyleItalic(false);
		renderGroup.setFontWeightBold(false);
		renderGroup.setTextAnchor(HTextAnchor.MIDDLE);
		renderGroup.setVTextAnchor(VTextAnchor.MIDDLE);
	}
	
	public Image createImage(GraphicalObject generalGlyph, String clazz){
		Image image = new Image("Image_" + generalGlyph.getId());
		image.setX(0);
		image.setX(0);
		image.setWidth(100);
		image.setHeight(100);
		image.setAbsoluteX(false);
		image.setAbsoluteY(false);		
		image.setAbsoluteWidth(false);
		image.setAbsoluteHeight(false);	
		
		// todo: horizontal or vertical?
		if (clazz.equals("or")){
			image.setHref("or-glyph.png");	
		} else if (clazz.equals("process")){
			image.setHref("process-glyph.png");	
		} else if (clazz.equals("macromolecule")){
			image.setHref("macromolecule-glyph.png");	
		} else if (clazz.equals("simple chemical")){
			image.setHref("simple-chemical-glyph.png");	
		} else if (clazz.equals("source and sink")){
			image.setHref("source-and-sink-glyph.png");
		} else if (clazz.equals("and")){
			image.setHref("and-glyph.png");
		} else if (clazz.equals("nucleic acid feature")){
			image.setHref("nucleic-acid-feature-glyph.png");
		} else if (clazz.equals("complex")){
			image.setHref("complex-glyph.png");
		} else if (clazz.equals("unit of information")){
			image.setHref("unit-of-information-glyph.png");
		} else if (clazz.equals("cardinality")){
			image.setHref("unit-of-information-glyph.png");
		} else if (clazz.equals("state variable")){
			image.setHref("state-variable-glyph.png");
		} else if (clazz.equals("biological activity")){
			image.setHref("biological-activity-glyph.png");
		} else if (clazz.equals("phenotype")){
			image.setHref("phenotype-glyph.png");
		} else if (clazz.equals("compartment")){
			image.setHref("compartment-glyph.png");
		}
		
		else if (clazz.equals("tag_left")){
			image.setHref("tag_left.png");
		} else if (clazz.equals("tag_right")){
			image.setHref("tag_right.png");
		}
		
		return image;
	}
	
	public Rectangle createRectangle(double x, double y, 
			double width, double height, 
			double rx, double ry, 
			boolean absoluteX, boolean absoluteY, 
			boolean absoluteRx, boolean absoluteRy,
			boolean absoluteW, boolean absoluteH) {
		Rectangle rectangle = new Rectangle();
		
		rectangle.setHeight(height);
		rectangle.setWidth(width);
		rectangle.setX(x);
		rectangle.setY(y);
		rectangle.setRx(rx);
		rectangle.setRy(ry);
		rectangle.setAbsoluteX(absoluteX);
		rectangle.setAbsoluteY(absoluteY);		
		rectangle.setAbsoluteRx(absoluteRx);
		rectangle.setAbsoluteRy(absoluteRy);		
		rectangle.setAbsoluteWidth(absoluteW);
		rectangle.setAbsoluteHeight(absoluteH);
		
		return rectangle;
	}
	
	public Ellipse createEllipse(double cx, double cy, double rx,
			boolean absoluteCx, boolean absoluteCy, boolean absoluteRx) {
		Ellipse ellipse = new Ellipse(3, 1);
		
		ellipse.setRx(rx);
		ellipse.setCx(cx);
		ellipse.setCy(cy);
		ellipse.setAbsoluteCx(absoluteCx);
		ellipse.setAbsoluteCy(absoluteCy);	
		ellipse.setAbsoluteRx(absoluteRx);	
		
		ellipse.setFill("white");
		
		return ellipse;
	}
	
	public Text createText(double x, double y, 
			boolean absoluteX, boolean absoluteY) {
		Text text = new Text();
		
		text.setAbsoluteX(absoluteX);
		text.setAbsoluteY(absoluteY);
		text.setX(x);
		text.setY(y);	
			
		return text;
	}
	
	public void display() {
		for (LineEnding lineEnding: sOutput.listOfLineEndings) {
			System.out.println("[renderGeneralGlyphs] lineEnding "+lineEnding.getId());
		}		
	}

}
