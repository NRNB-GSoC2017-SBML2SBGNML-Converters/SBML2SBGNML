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
import org.sbml.jsbml.ext.render.Polygon;
import org.sbml.jsbml.ext.render.Rectangle;
import org.sbml.jsbml.ext.render.RenderConstants;
import org.sbml.jsbml.ext.render.RenderGraphicalObjectPlugin;
import org.sbml.jsbml.ext.render.RenderGroup;
import org.sbml.jsbml.ext.render.RenderLayoutPlugin;
import org.sbml.jsbml.ext.render.RenderPoint;
import org.sbml.jsbml.ext.render.Text;
import org.sbml.jsbml.ext.render.VTextAnchor;

public class SBGNML2SBMLRender {
	SWrapperModel sWrapperModel;
	SBGNML2SBMLOutput sOutput;
	SBGNML2SBMLUtil sUtil;
	
	public SBGNML2SBMLRender(SWrapperModel sWrapperModel, SBGNML2SBMLOutput sOutput, SBGNML2SBMLUtil sUtil) {
		this.sWrapperModel = sWrapperModel;
		this.sOutput = sOutput;
		this.sUtil = sUtil;
		//createColourDefinitions();
		
		//createLineEndings();
	}
	
	public void renderGeneralGlyphs() {
		//todo: generalGlyphs as species (for a logic operator)?
		// as reactions might not work
		SWrapperGeneralGlyph sWrapperGeneralGlyph;
		
		for (String key : sWrapperModel.listOfWrapperGeneralGlyphs.keySet()){
			sWrapperGeneralGlyph = sWrapperModel.getWrapperGeneralGlyph(key);
			LocalStyle localStyle = createStyle(sWrapperGeneralGlyph.generalGlyph, sWrapperGeneralGlyph.clazz);
			if (sWrapperGeneralGlyph.isAnnotation) {
				Dimensions dim = sWrapperGeneralGlyph.generalGlyph.getBoundingBox().getDimensions();
				Point startPoint = sWrapperGeneralGlyph.calloutPoint;
				Point bbPosition = sWrapperGeneralGlyph.generalGlyph.getBoundingBox().getPosition();
				Point endPoint1 = new Point(bbPosition.getX()+0.3*dim.getWidth(), bbPosition.getY()+dim.getHeight());
				Point endPoint2 = new Point(bbPosition.getX()+0.4*dim.getWidth(),bbPosition.getY()+dim.getHeight());
				createTriangle(localStyle, startPoint, endPoint1, endPoint2);
			}
			
			renderReferenceGlyphs(sWrapperGeneralGlyph);
		}
	}	
	
	public void createTriangle(LocalStyle localStyle, Point p1, Point p2, Point p3){
		Polygon polygon = new Polygon();
		ListOf<RenderPoint> elements = polygon.getListOfElements();
				
		elements.add(createRenderPoint(p1.getX(), p1.getY(), true, true));
		elements.add(createRenderPoint(p2.getX(), p2.getY(), true, true));
		elements.add(createRenderPoint(p3.getX(), p3.getY(), true, true));
		
		RenderGroup renderGroup = localStyle.getGroup();
		renderGroup.addElement(polygon);
	}
	
	public RenderPoint createRenderPoint(double x, double y, boolean absoluteX, boolean absoluteY){
		RenderPoint renderPoint = new RenderPoint();
		renderPoint.setX(x);
		renderPoint.setY(y);
		renderPoint.setAbsoluteX(absoluteX);
		renderPoint.setAbsoluteY(absoluteY);	
		
		return renderPoint;
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
			LocalStyle localStyle = createStyle(sWrapperSpeciesGlyph.speciesGlyph, sWrapperSpeciesGlyph.clazz);
			
			if (sWrapperSpeciesGlyph.hasClone){
				//addCloneText(localStyle, sWrapperSpeciesGlyph.speciesGlyph, sWrapperSpeciesGlyph.cloneText);
			}
		}		
		
		SWrapperQualitativeSpecies sWrapperQualitativeSpecies;
		for (String key : sWrapperModel.listOfSWrapperQualitativeSpecies.keySet()){
			sWrapperQualitativeSpecies = sWrapperModel.getSWrapperQualitativeSpecies(key);
			createStyle(sWrapperQualitativeSpecies.speciesGlyph, sWrapperQualitativeSpecies.clazz);
		}	
	}
	
	public void renderTextGlyphs() {
		
		for (TextGlyph tg : sOutput.layout.getListOfTextGlyphs()){

			if (sWrapperModel.textSourceMap.containsKey(tg.getId())){
				LocalStyle localStyle = createStyle(tg, "top_text");
			} else {
				LocalStyle localStyle = createStyle(tg, "text");
			}
			

		}		
		
	}
	
	public void addCloneText(LocalStyle localStyle, SpeciesGlyph speciesGlyph, String cloneText) {
		// the 0 and 80 is not used?
		Text text = createText(0, 80, false, false);
		text.setFontFamily(FontFamily.MONOSPACE);
		text.setTextAnchor(HTextAnchor.MIDDLE);
		text.setVTextAnchor(VTextAnchor.BOTTOM);	
		text.setName(cloneText);	
		
		RenderGroup renderGroup = localStyle.getGroup();
		renderGroup.setStroke("white");
		renderGroup.addElement(text);
		// todo: how to put text value in element?
	}
	
	public void renderReactionGlyphs() {
		SWrapperReactionGlyph sWrapperReactionGlyph;
		for (String key : sWrapperModel.listOfWrapperReactionGlyphs.keySet()){
			sWrapperReactionGlyph = sWrapperModel.getWrapperReactionGlyph(key);
			createStyle(sWrapperReactionGlyph.reactionGlyph, sWrapperReactionGlyph.clazz);
			renderSpeciesReferenceGlyphs(sWrapperReactionGlyph);
			
			if (sWrapperReactionGlyph.clazz.equals("uncertain process")){
				TextGlyph tg = sUtil.createJsbmlTextGlyph(sWrapperReactionGlyph.reactionGlyph, "?");
				sOutput.addTextGlyph(tg);
			}
		}		
	}
	
	public void renderSpeciesReferenceGlyphs(SWrapperReactionGlyph sWrapperReactionGlyph) {
		Arc arc;
		SpeciesReferenceGlyph speciesReferenceGlyph;
		//System.out.println("===renderSpeciesReferenceGlyphs "+sWrapperReactionGlyph.speciesReferenceGlyphs.keySet().size());
		
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
			arc = sWrapperGeneralGlyph.arcs.get(arcKey).arc;
			
			createStyle((GraphicalObject) referenceGlyph, arc);
		}		
	}
	
	public LocalStyle createStyle(GraphicalObject graphicalObject, String clazz) {
		RenderGroup renderGroup;
		
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin;
		LocalRenderInformation localRenderInformation = sOutput.localRenderInformation;
		Layout layout = sOutput.layout;
		
		String styleId = findObjectRole(clazz);
		
//		String styleId = "LocalStyle_" + graphicalObject.getId();
//		renderGroup = new RenderGroup(layout.getLevel(), layout.getVersion());
//		initializeDefaultRenderGroup(renderGroup);
//		localStyle = new LocalStyle(styleId, layout.getLevel(), layout.getVersion(), renderGroup);
//		localRenderInformation.addLocalStyle(localStyle);
//		localStyle.getRoleList().add(styleId);
		
		renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) graphicalObject.getPlugin(RenderConstants.shortLabel);
		renderGraphicalObjectPlugin.setObjectRole(styleId);		
				
//		Image image = createImage(graphicalObject, clazz);
//		renderGroup.addElement(image);	
		
		// todo: return a Style
		
		for (LocalStyle localStyle: sOutput.listOfStyles_temp){
			if (localStyle.getId().equals(styleId)){
				return localStyle;
			}
		}
		

		
		return null;
	}
	
	public void createStyle(GraphicalObject graphicalObject, Arc arc) {
		RenderGroup renderGroup;
		LocalStyle localStyle;
		RenderGraphicalObjectPlugin renderGraphicalObjectPlugin;
		LocalRenderInformation localRenderInformation = sOutput.localRenderInformation;
		Layout layout = sOutput.layout;
		
		String styleId = findObjectRole(arc.getClazz());
		
//		String styleId = "LocalStyle_" + graphicalObject.getId();
//		renderGroup = new RenderGroup(layout.getLevel(), layout.getVersion());
//		initializeDefaultRenderGroup(renderGroup);
//		localStyle = new LocalStyle(styleId, layout.getLevel(), layout.getVersion(), renderGroup);
//		localRenderInformation.addLocalStyle(localStyle);
//		localStyle.getRoleList().add(styleId);
		
		renderGraphicalObjectPlugin = (RenderGraphicalObjectPlugin) graphicalObject.getPlugin(RenderConstants.shortLabel);
		renderGraphicalObjectPlugin.setObjectRole(styleId);		
		
		
		

	}	
	
//	public void createColourDefinitions() {
//		LocalRenderInformation localRenderInformation = sOutput.localRenderInformation;
//		Layout layout = sOutput.layout;
//		
//		ColorDefinition colorDefinition;
//		colorDefinition = new ColorDefinition(layout.getLevel(), layout.getVersion());
//		colorDefinition.setId("white");
//		colorDefinition.setValue(Color.decode("#FFFFFF"));		
//		localRenderInformation.addColorDefinition(colorDefinition);		
//		
//		colorDefinition = new ColorDefinition(layout.getLevel(), layout.getVersion());
//		colorDefinition.setId("black");
//		colorDefinition.setValue(Color.decode("#000000"));		
//		localRenderInformation.addColorDefinition(colorDefinition);		
//	}
	
//	public void createLineEndings(ListOf<LineEnding>  listOfLineEndings) {
//		sOutput.addListOfLineEndings(listOfLineEndings);
//	}
	
//	public void initializeDefaultRenderGroup(RenderGroup renderGroup) {
//		renderGroup.setStroke("black");
//		renderGroup.setStrokeWidth(3);
//		renderGroup.setFillRule(FillRule.NONZERO);
//		renderGroup.setFontSize((short) 12);
//		renderGroup.setFontFamily(FontFamily.SANS_SERIF);
//		renderGroup.setFontStyleItalic(false);
//		renderGroup.setFontWeightBold(false);
//		renderGroup.setTextAnchor(HTextAnchor.MIDDLE);
//		renderGroup.setVTextAnchor(VTextAnchor.MIDDLE);
//	}
	
	public String findObjectRole(String clazz){
//		Image image = new Image("Image_" + generalGlyph.getId());
//		image.setX(0);
//		image.setX(0);
//		image.setWidth(100);
//		image.setHeight(100);
//		image.setAbsoluteX(false);
//		image.setAbsoluteY(false);		
//		image.setAbsoluteWidth(false);
//		image.setAbsoluteHeight(false);	
		
		String styleId = null;
		
		// todo: horizontal or vertical?
		if (clazz.equals("or")){
			styleId = "or";
		} else if (clazz.equals("and")){
			styleId = "and";
		} else if (clazz.equals("not")){
			styleId = "not";
		} 
		
		else if (clazz.equals("process")){
			styleId = "SBO0000167";
		} else if (clazz.equals("association")){
			styleId = "SBO0000177";
		} else if (clazz.equals("dissociation")){
			styleId = "SBO0000180";
		} else if (clazz.equals("omitted process")){
			styleId = "SBO0000397";
		} else if (clazz.equals("uncertain process")){
			styleId = "SBO0000396";
		}
		
		else if (clazz.equals("macromolecule")){
			styleId = "SBO0000245";
		} else if (clazz.equals("simple chemical")){
			styleId = "SBO0000247";
		} else if (clazz.equals("source and sink")){
			styleId = "SBO0000291";
		} else if (clazz.equals("nucleic acid feature")){
			styleId = "SBO0000354";
		} else if (clazz.equals("complex")){
			styleId = "SBO0000253";
		} else if (clazz.equals("perturbing agent")){
			styleId = "SBO0000405";
		 }else if (clazz.equals("unspecified entity")){
			styleId = "SBO0000285";
		}
		
		else if (clazz.equals("biological activity")){
			
		} else if (clazz.equals("phenotype")){
			styleId = "SBO0000358";
		} else if (clazz.equals("annotation")){
		} 
		
		else if (clazz.equals("simple chemical_clone")){
			styleId = "SBO0000247clone";
		} else if (clazz.equals("macromolecule_clone")){
			styleId = "SBO0000245clone";
		} else if (clazz.equals("nucleic acid feature_clone")){
			styleId = "SBO0000354clone";
		} 
		
		else if (clazz.equals("macromolecule multimer_clone")){
			styleId = "SBO0000245multimerclone";
		} else if (clazz.equals("simple chemical multimer_clone")){
			styleId = "SBO0000247multimerclone";
		} else if (clazz.equals("nucleic acid feature multimer_clone")){
			styleId = "SBO0000354multimerclone";
		} 
		
		else if (clazz.equals("simple chemical multimer")){
			styleId = "SBO0000247multimer";
		} else if (clazz.equals("macromolecule multimer")){
			styleId = "SBO0000245multimer";
		} else if (clazz.equals("nucleic acid feature multimer")){
			styleId = "SBO0000354multimer";
		} else if (clazz.equals("complex multimer")){
			styleId = "SBO0000253multimer";
		}
		
		else if (clazz.equals("unit of information")){
			styleId = "unitofinfo";
		} else if (clazz.equals("cardinality")){
			styleId = "unitofinfo";
		} else if (clazz.equals("state variable")){
			styleId = "statevar";
		} 
		
		else if (clazz.equals("compartment")){
			styleId = "SBO0000289";
		} else if (clazz.equals("submap")){
			styleId = "SBO0000395";
		} 
		
		else if (clazz.equals("tag_left")){
			styleId = "Tagleft";
		} else if (clazz.equals("tag_right")){
			styleId = "Tagright";
		} else if (clazz.equals("tag_up")){
			styleId = "Tagup";
		} else if (clazz.equals("tag_down")){
			styleId = "Tagdown";
		}
		
		else if (clazz.equals("terminal_left")){
			styleId = "Tagleft";
		} else if (clazz.equals("terminal_right")){
			styleId = "Tagright";
		} else if (clazz.equals("terminal_up")){
			styleId = "Tagup";
		} else if (clazz.equals("terminal_down")){
			styleId = "Tagdown";
		}		
		
		if (clazz.equals("catalysis")){
			sOutput.lineEndingsInModel.put("activator", null);
			styleId = "SBO0000172";
		} else if (clazz.equals("production")){
			sOutput.lineEndingsInModel.put("product", null);
			styleId = "product";
		} else if (clazz.equals("consumption")){	
			styleId = "substrate";
		} else if (clazz.equals("stimulation")){
			sOutput.lineEndingsInModel.put("stimulation", null);
			styleId = "SBO0000170";
		} else if (clazz.equals("necessary stimulation")){
			sOutput.lineEndingsInModel.put("necessary_stimulation", null);
			styleId = "SBO0000171";
		} else if (clazz.equals("unknown influence")){
			sOutput.lineEndingsInModel.put("modulation", null);
			styleId = "SBO0000168";
		} else if (clazz.equals("inhibition")){
			sOutput.lineEndingsInModel.put("inhibitor", null);
			styleId = "SBO0000169";
		} else if (clazz.equals("modulation")){
			sOutput.lineEndingsInModel.put("modulation", null);
			styleId = "SBO0000168";
		}
		
		else if (clazz.equals("equivalence arc")){
			styleId = "equivalence";
		} else if (clazz.equals("logic arc")){
			styleId = "SBO0000398";
		}
		
		
		else if (clazz.equals("text")){
			styleId = "defaultText";
		} 		else if (clazz.equals("top_text")){
			styleId = "topText";
		}
		
		
		sOutput.stylesInModel.put(styleId, null);
		//System.out.println("stylesInModel ===+" + styleId);
		
		
		return styleId;
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
