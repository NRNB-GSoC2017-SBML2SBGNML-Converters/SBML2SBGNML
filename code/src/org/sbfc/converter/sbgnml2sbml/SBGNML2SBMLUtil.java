package org.sbfc.converter.sbgnml2sbml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Label;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Port;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.CVTerm.Type;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.GeneralGlyph;
import org.sbml.jsbml.ext.layout.GraphicalObject;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.ReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.ext.qual.FunctionTerm;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.InputTransitionEffect;
import org.sbml.jsbml.ext.qual.Output;
import org.sbml.jsbml.ext.qual.OutputTransitionEffect;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;

public class SBGNML2SBMLUtil {
	int debugMode;
	int level;
	int version;
	
	int createOneCurveError = 0;
	
	SBGNML2SBMLUtil(int level, int version) {
		this.level = level;
		this.version = version;
	}
	
	public QualitativeSpecies createQualitativeSpecies(String speciesId, String name, String clazz, 
			boolean addAnnotation, boolean addSBO) {
		QualitativeSpecies species;
		species = new QualitativeSpecies(speciesId, name, level, version);
		
//		if (addAnnotation){
//			addAnnotation(species, clazz);
//		}
//		if (addSBO){
//			addSBO(species, clazz);	
//		}
		
		return species;
	}
	
	public Species createJsbmlSpecies(String speciesId, String name, String clazz, 
			boolean addAnnotation, boolean addSBO) {
		Species species;
		species = new Species(speciesId, name, level, version);
		
		// if no [] info provided
		species.setInitialConcentration(1.0);
		
		// todo: do this for other objects in sbml (e.g. reaction, speciesreference)
		if (addAnnotation){
			// todo change to something else
			addAnnotation(species, clazz, Qualifier.BQB_IS_VERSION_OF);
		}
		if (addSBO){
			addSBO(species, clazz);	
		}
		
		return species;
	}	
	
	public SpeciesGlyph createJsbmlSpeciesGlyph(String speciesId, String name, String clazz, 
			Species species, boolean createBoundingBox, Bbox bbox) {
		SpeciesGlyph speciesGlyph;
		BoundingBox boundingBox;
		
		speciesGlyph = new SpeciesGlyph();
		speciesGlyph.setId("SpeciesGlyph_" + speciesId);
		
		// QualitativeSpecies is not Species
		if (species != null){
			speciesGlyph.setSpecies(species);
		}
				
		if (createBoundingBox) {
			boundingBox = new BoundingBox();
			// todo: horizontal or vertical orientation?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			speciesGlyph.setBoundingBox(boundingBox);				
		}
		return speciesGlyph;
	}
	
	public GeneralGlyph createJsbmlGeneralGlyph(String id, boolean createBoundingBox, Bbox bbox) {
		GeneralGlyph generalGlyph;
		BoundingBox boundingBox;

		generalGlyph = new GeneralGlyph();
		generalGlyph.setId("GeneralGlyph_" + id);
		
		if (createBoundingBox) {
			boundingBox = new BoundingBox();
			// todo: horizontal or vertical orientation?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			generalGlyph.setBoundingBox(boundingBox);					
		}

		return generalGlyph;
	}
	
	/**
	 * Create a TextGlyph using values from a <code>SpeciesGlyph</code> and its associated <code>Species</code>.
	 * 
	 * @param <code>Species</code> species
	 * @param <code>SpeciesGlyph</code> speciesGlyph
	 */		
	public TextGlyph createJsbmlTextGlyph(NamedSBase species, SpeciesGlyph speciesGlyph, Bbox labelBbox) {
		TextGlyph textGlyph;
		String id;
		BoundingBox boundingBoxText;
		BoundingBox boundingBoxSpecies;
		
		textGlyph = new TextGlyph(level, version);
		id = "TextGlyph_" + species.getId();
		textGlyph.setId(id);
		textGlyph.setOriginOfText(species);
		textGlyph.setGraphicalObject(speciesGlyph);
		
		boundingBoxText = new BoundingBox();
		
		if (labelBbox == null){
		boundingBoxSpecies = speciesGlyph.getBoundingBox();
		boundingBoxText.setDimensions(boundingBoxSpecies.getDimensions());
		boundingBoxText.setPosition(boundingBoxSpecies.getPosition());
		textGlyph.setBoundingBox(boundingBoxText);
		} else {
			boundingBoxText.createDimensions(labelBbox.getW(), labelBbox.getH(), 0);
			boundingBoxText.createPosition(labelBbox.getX(), labelBbox.getY(), 0);
			textGlyph.setBoundingBox(boundingBoxText);	
		}
				
		return textGlyph;
	}	
	
	
	public TextGlyph createJsbmlTextGlyph(GraphicalObject generalGlyph, String text, Bbox labelBbox) {
		TextGlyph textGlyph;
		String id;
		BoundingBox boundingBoxText;
		BoundingBox boundingBoxGeneralGlyph;
		
		textGlyph = new TextGlyph(level, version);
		id = "TextGlyph_" + generalGlyph.getId();
		textGlyph.setId(id);
		textGlyph.setGraphicalObject(generalGlyph);
		textGlyph.setText(text);
		
		boundingBoxText = new BoundingBox();
		
		if (labelBbox == null){
			boundingBoxGeneralGlyph = generalGlyph.getBoundingBox();
			boundingBoxText.setDimensions(boundingBoxGeneralGlyph.getDimensions());
			boundingBoxText.setPosition(boundingBoxGeneralGlyph.getPosition());
			textGlyph.setBoundingBox(boundingBoxText);			
		} else {
			// todo: horizontal or vertical orientation?
			boundingBoxText.createDimensions(labelBbox.getW(), labelBbox.getH(), 0);
			boundingBoxText.createPosition(labelBbox.getX(), labelBbox.getY(), 0);
			textGlyph.setBoundingBox(boundingBoxText);	
		}
				
		return textGlyph;
	}		
	
	
	public String getText(Glyph glyph){
		if (glyph.getLabel() != null) {

			return glyph.getLabel().getText();
		}
		if (glyph.getClazz().equals("state variable")){
			if (glyph.getState() != null){
				return glyph.getState().getValue() + "@" + glyph.getState().getVariable();
			}
			// todo: add more cases
			else {
				//System.out.println("======getText no state");
				return "";
			}
		}
		return "";
	}
	
	public String getClone(Glyph glyph) {
		String text = new String();
		try {
			Glyph.Clone clone = glyph.getClone();
			Label label = clone.getLabel();
			try {
				text = new String(String.copyValueOf(label.getText().toCharArray())) ;
			}
			catch (NullPointerException e) {
				
			}
		}
		catch (NullPointerException e) {
			return null;
		}	
		
		return text;
	}
	
	public Reaction createJsbmlReaction(String reactionId) {
		Reaction reaction;
		
		reaction = new Reaction();
		reaction.setId(reactionId);
		
		return reaction;
	}
	
	public ReactionGlyph createJsbmlReactionGlyph(String reactionId, String name, String clazz, 
			Reaction reaction, boolean createBoundingBox, Bbox bbox) {
		ReactionGlyph reactionGlyph;
		BoundingBox boundingBox;
		
		reactionGlyph = new ReactionGlyph();
		reactionGlyph.setId("ReactionGlyph_" + reactionId);
		reactionGlyph.setReaction(reaction);
		
		if (createBoundingBox) {
			boundingBox = new BoundingBox();
			// todo: horizontal or vertical orientation?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			reactionGlyph.setBoundingBox(boundingBox);				
		}
		return reactionGlyph;
	}	
	
	public Dimensions createDimensions(BoundingBox boundingBox, Bbox bbox){
		Dimensions dimension;
		
		boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
		
		return boundingBox.getDimensions();
	}
	public Point createPoint(BoundingBox boundingBox, Bbox bbox){
		Point point;
		
		boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);	
		
		return boundingBox.getPosition();
	}
	public void createBoundingBox(GraphicalObject graphicalObject, Glyph glyph){
		BoundingBox boundingBox = new BoundingBox();
		Bbox bbox = glyph.getBbox();
		
		graphicalObject.setBoundingBox(boundingBox);
		createDimensions(boundingBox, bbox);
		createPoint(boundingBox, bbox);
	}	
	
	/**
	 * Create a temporary <code>Curve</code> for the given <code>ReactionGlyph</code>. 
	 * This Curve has incorrect Start and End Points. 
	 * The values of the <code>Curve</code> will be modified later;
	 */	
	public void createReactionGlyphCurve(ReactionGlyph reactionGlyph, Glyph glyph) {
		Curve curve;
		CurveSegment curveSegment;
		Point point;
		Bbox bbox;
		
		curve = new Curve();
		//curveSegment = new LineSegment();
		bbox = glyph.getBbox();
		point = new Point(bbox.getX(), bbox.getY());
		//curveSegment.setStart(point);
		point = new Point(bbox.getX()+bbox.getW(), bbox.getY()+bbox.getH());
		//curveSegment.setEnd(point);
		//curve.addCurveSegment(curveSegment);
		reactionGlyph.setCurve(curve);		
	}
		
	public SpeciesReference createSpeciesReference(Reaction reaction, Species species, 
			String speciesReferenceId) {
		SpeciesReference speciesReference;
		
		speciesReference = new SpeciesReference();
		speciesReference.setId(speciesReferenceId);
		speciesReference.setSpecies(species);
	
		return speciesReference;
	}	
	
	public ModifierSpeciesReference createModifierSpeciesReference(Reaction reaction, Species species, 
			String speciesReferenceId) {
		ModifierSpeciesReference speciesReference;
		
		speciesReference = new ModifierSpeciesReference();
		speciesReference.setId(speciesReferenceId);
		speciesReference.setSpecies(species);
	
		return speciesReference;
	}		
	
	public Transition createTransition(String id, 
			QualitativeSpecies inputQualitativeSpecies, QualitativeSpecies outputQualitativeSpecies){
		Transition transition = new Transition();
		Input input;
		Output output;
		String inputId;
		String outputId;
		FunctionTerm functionTerm;
		
		if (inputQualitativeSpecies != null){
			inputId = "Input_" + inputQualitativeSpecies.getId() + "_in_" + id;
			input = new Input(inputId, inputQualitativeSpecies, InputTransitionEffect.none);
			transition.addInput(input);
		}
		
		if (outputQualitativeSpecies != null){
			outputId = "Output_" + outputQualitativeSpecies.getId() + "_in_" + id; 
			output = new Output(outputId, outputQualitativeSpecies, OutputTransitionEffect.assignmentLevel);
			transition.addOutput(output);
		}
		
		functionTerm = new FunctionTerm();
		functionTerm.setDefaultTerm(true);
		functionTerm.setResultLevel(0);
		transition.addFunctionTerm(functionTerm);
		
		// todo: only create one if both input and output is known
		functionTerm = new FunctionTerm();
		functionTerm.setDefaultTerm(false);
		functionTerm.setResultLevel(1);
		transition.addFunctionTerm(functionTerm);
		
		return transition;
	}
	
	public Transition createTransition(String id){
		Transition transition = new Transition(id);
		
		return transition;
	}
	
	public Input addInputToTransition(Transition transition, QualitativeSpecies inputQualitativeSpecies){
		Input input;
		String inputId;		

		inputId = "Input_" + inputQualitativeSpecies.getId() + "_in_" + transition.getId();
		input = new Input(inputId, inputQualitativeSpecies, InputTransitionEffect.none);
		transition.addInput(input);
		
		return input;
	}
	
	public Output addOutputToTransition(Transition transition, QualitativeSpecies outputQualitativeSpecies){
		Output output;
		String outputId;
		
		outputId = "Output_" + outputQualitativeSpecies.getId() + "_in_" + transition.getId(); 
		output = new Output(outputId, outputQualitativeSpecies, OutputTransitionEffect.assignmentLevel);
		transition.addOutput(output);	
		
		return output;
	}
	
	public FunctionTerm addFunctionTermToTransition(Transition transition, boolean setDefaultTerm, int resultLevel){
		FunctionTerm functionTerm;
		
		functionTerm = new FunctionTerm();
		functionTerm.setDefaultTerm(setDefaultTerm);
		functionTerm.setResultLevel(resultLevel);
		transition.addFunctionTerm(functionTerm);
		
		return functionTerm;
	}
	
	public ASTNode createMath(ASTNode parentMath, String type){
		//ASTNode math = functionTerm.getMath();
		ASTNode math = null;
//		Type		
//		LOGICAL_AND
//		LOGICAL_OR
//		LOGICAL_NOT
		if (type.equals("and")){
			math = new ASTNode(ASTNode.Type.LOGICAL_AND);
		} if (type.equals("or")){
			math = new ASTNode(ASTNode.Type.LOGICAL_OR);
		} if (type.equals("not")){
			math = new ASTNode(ASTNode.Type.LOGICAL_NOT);
		} 
		
		parentMath.insertChild(0, math);
				
		return math;
	}
	
	public ASTNode createMath(String type, FunctionTerm mathContainer){
		//ASTNode math = functionTerm.getMath();
		ASTNode math = null;
//		Type		
//		LOGICAL_AND
//		LOGICAL_OR
//		LOGICAL_NOT
		if (type.equals("and")){
			math = new ASTNode(ASTNode.Type.LOGICAL_AND);
		} if (type.equals("or")){
			math = new ASTNode(ASTNode.Type.LOGICAL_OR);
		} if (type.equals("not")){
			math = new ASTNode(ASTNode.Type.LOGICAL_NOT);
		} 
		
		math.setParentSBMLObject(mathContainer);
		
		return math;
	}
	
	/**
	 * Create a <code>SpeciesReferenceGlyph</code> using values from an SBGN <code>Arc</code>. 
	 * Associate the <code>SpeciesReferenceGlyph</code> with a <code>SpeciesGlyph</code> and 
	 * a <code>SpeciesReference</code>.
	 * 
	 * @param <code>String</code> id
	 * @param <code>Arc</code> arc
	 * @param <code>SpeciesReference</code> speciesReference
	 * @param <code>Glyph</code> speciesGlyph
	 * @return <code>SpeciesReferenceGlyph</code> speciesReferenceGlyph
	 */		
	public SpeciesReferenceGlyph createOneSpeciesReferenceGlyph(String id, Arc arc, 
			SimpleSpeciesReference speciesReference, Glyph speciesGlyph, SBGNML2SBMLOutput sOutput) {
		SpeciesReferenceGlyph speciesReferenceGlyph;
		
		speciesReferenceGlyph = new SpeciesReferenceGlyph();
		speciesReferenceGlyph.setId("SpeciesReferenceGlyph_"+id);
		speciesReferenceGlyph.setRole(findReactionRole(arc.getClazz()));
		speciesReferenceGlyph.setSpeciesGlyph("SpeciesGlyph_"+speciesGlyph.getId());
		speciesReferenceGlyph.setSpeciesReference(speciesReference);	
		sOutput.numOfSpeciesReferences++;			
		
		return speciesReferenceGlyph;
	}
	
	/**
	 * Create an SBML <code>Curve</code> from values in an SBGN <code>Arc</code>. 
	 * TODO: need to handle more complex cases where an Arc consists of multiple parts
	 */		
	public Curve createOneCurve(Arc arc) {
		Curve curve;
		CurveSegment curveSegment;
		//Point point;
		
		Arc.Start start;
		Arc.End end;
		List<Arc.Next> listOfNext;
		List<org.sbgn.bindings.Point> points;
		
		List<Point> curvePoints = new ArrayList<Point>();
		
		start = arc.getStart();
		//point = new Point(start.getX(), start.getY());
		curvePoints.add(new Point(start.getX(), start.getY()));
		
		listOfNext = arc.getNext();
		//System.out.format("=====Arc id=%s \n", arc.getId());
		for (Arc.Next next: listOfNext){
			//System.out.format("Next: (x=%s,y=%s) \n", Float.toString(next.getX()), Float.toString(next.getY()));
			//point = new Point(next.getX(), next.getY());
			
			points = next.getPoint();
			
			if (points.size() > 0){
//				for (org.sbgn.bindings.Point p: points){
//					//System.out.format("    Point: (x=%s,y=%s) \n", Float.toString(p.getX()), Float.toString(p.getY()));
//				}		
				SBGNWrapperPoint sWrapperPoint = new SBGNWrapperPoint(next.getX(), next.getY());
				sWrapperPoint.addbasePoint(points);
				curvePoints.add(sWrapperPoint);
				
			} else {
				curvePoints.add(new Point(next.getX(), next.getY()));
			}

			

		}
		
		end = arc.getEnd();
		//point = new Point(end.getX(), end.getY());
		points = end.getPoint();
		if (points.size() > 0){
			SBGNWrapperPoint sWrapperPoint = new SBGNWrapperPoint(end.getX(), end.getY());
			sWrapperPoint.addbasePoint(points);
			curvePoints.add(sWrapperPoint);
		} else {
			curvePoints.add(new Point(end.getX(), end.getY()));
		}
		
		curve = new Curve();
		Point startPoint;
		Point endPoint;
		for (int i = 0; i < curvePoints.size(); i++){
			
			// last Point
			if (i == curvePoints.size() - 1){
				break;
			}
			
			curveSegment = null;
			
			startPoint = curvePoints.get(i);
			if (startPoint instanceof SBGNWrapperPoint){
				startPoint = ((SBGNWrapperPoint) startPoint).targetPoint.clone();
			}
			
			endPoint = curvePoints.get(i + 1);
			if (endPoint instanceof SBGNWrapperPoint){
				Point endPointNew = ((SBGNWrapperPoint) endPoint).targetPoint.clone();
				Point basePoint1 = ((SBGNWrapperPoint) endPoint).basePoint1.clone();
				Point basePoint2 = ((SBGNWrapperPoint) endPoint).basePoint2.clone();
				
				curveSegment = new CubicBezier();
				curveSegment.setStart(startPoint);
				curveSegment.setEnd(endPointNew);	
				
				((CubicBezier) curveSegment).setBasePoint1(basePoint1);
				((CubicBezier) curveSegment).setBasePoint2(basePoint2);
				
			} else {
				endPoint = curvePoints.get(i + 1).clone();
				curveSegment = new LineSegment();
				
				curveSegment.setStart(startPoint);
				curveSegment.setEnd(endPoint);	
			}	
			
			curve.addCurveSegment(curveSegment);				
		}
		
		if (curve.getCurveSegmentCount() == 0){
			createOneCurveError ++;
			System.out.format("! createOneCurve sbml="+curvePoints.size());
			for (Arc.Next next: listOfNext){
				points = next.getPoint();
				
				System.out.format(" sbgn.next="+next.getPoint().size());
			}
			System.out.format(" sbgn.end="+end.getPoint().size()+" \n");

		}
		
		return curve;
	}	
	
	public Compartment createJsbmlCompartment(String compartmentId, String name) {
		Compartment compartment = new Compartment(compartmentId, name, level, version);
		return compartment;
	}
	
	public CompartmentGlyph createJsbmlCompartmentGlyph(Glyph glyph, String compartmentId, Compartment compartment,
			boolean createBoundingBox) {
		CompartmentGlyph compartmentGlyph;
		Bbox bbox;
		BoundingBox boundingBox;
		
		compartmentGlyph = new CompartmentGlyph();
		compartmentGlyph.setId("CompartmentGlyph_" + compartmentId);
		compartmentGlyph.setCompartment(compartment);
		
		if (createBoundingBox){
			bbox = glyph.getBbox();
			boundingBox = new BoundingBox();
			// todo: horizontal?
			boundingBox.createDimensions(bbox.getW(), bbox.getH(), 0);
			boundingBox.createPosition(bbox.getX(), bbox.getY(), 0);
			compartmentGlyph.setBoundingBox(boundingBox);			
		}
		return compartmentGlyph;
	}
	
	public void setCompartmentOrder(CompartmentGlyph compartmentGlyph, Glyph glyph) {
//		System.out.println(compartmentGlyph.getId());
//		System.out.println(glyph.getId());
		
		try {
			float order = glyph.getCompartmentOrder();
			if ((Object) order != null){
				compartmentGlyph.setOrder(order);
			}
		} catch (NullPointerException e) {
			
		}
	}
	
	// todo: move to a ModelCompleter
	public void createDefaultCompartment(Model modelObject) {
		String compartmentId = "DefaultCompartment_01";
		Compartment compartment = new Compartment(compartmentId);
		modelObject.getListOfCompartments().add(compartment);
		
		for (Species species: modelObject.getListOfSpecies()){
			if (species.getCompartment() == ""){
				species.setCompartment(compartment);	
			}
		}
	}
	
	
	public ReferenceGlyph createOneReferenceGlyph(String id, Arc arc, ModifierSpeciesReference reference, 
			SBase object) {
		ReferenceGlyph referenceGlyph;
		
		referenceGlyph = new ReferenceGlyph();
		referenceGlyph.setId("ReferenceGlyph_" + id);
		referenceGlyph.setGlyph(object.getId());
		
		// does not work because we can't create a ModifierSpeciesReference. 
		// A ModifierSpeciesReference needs to associate with a Species, but we don't have a Species
		//referenceGlyph.setReference(reference.getId());

		return referenceGlyph;
	}	
	
	
	
	public Boolean isProcessNode(String clazz) {
		if (clazz.equals("process")) {
			return true;
		} else if (clazz.equals("omitted process")) {
			return true;
		} else if (clazz.equals("uncertain process")) {
			return true;
		} else if (clazz.equals("association")) {
			return true;
		} else if (clazz.equals("dissociation")) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isCompartment(String clazz) {
		if (clazz.equals("compartment")) {
			return true;
		}
		return false;
	}
	
	public Boolean isLogicOperator(String clazz) {
		if (clazz.equals("and")) {
			return true;
		} else if (clazz.equals("or")) {
			return true;
		} else if (clazz.equals("not")) {
			return true;
		}
		return false;
	}
	

	public boolean isTag(String clazz) {
		if (clazz.equals("tag")) {
			return true;
		} 
//		else if (clazz.equals("terminal")){
//			System.out.println("terminal");
//			return true;
//		}
		
		return false;
	}	
	
	public Boolean isAnnotation(String clazz) {
		if (clazz.equals("annotation")) {
			return true;
		}
		return false;
	}
	
	public Boolean isEntityPoolNode(String clazz){
		if (clazz.equals("unspecified entity")) {
			return true;
		} else if (clazz.equals("simple chemical")) {
			return true;
		} else if (clazz.equals("macromolecule")) {
			return true;
		} else if (clazz.equals("nucleic acid feature")) {
			return true;
		} else if (clazz.equals("complex multimer")) {
			return true;
		} else if (clazz.equals("complex")) {
			return true;
		} else if (clazz.equals("macromolecule multimer")) {
			return true;
		} else if (clazz.equals("nucleic acid feature multimer")) {
			return true;
		} else if (clazz.equals("simple chemical multimer")) {
			return true;
		} else if (clazz.equals("source and sink")) {
			return true;
		} else if (clazz.equals("perturbing agent")) {
			return true;
		} 
		
		else if (clazz.equals("biological activity")) {
			return true;
		} else if (clazz.equals("phenotype")) {
			return true;
		} else if (clazz.equals("submap")) {
			return true;
		} else {
			return false;
		}		
	}
	
	public Boolean isGlyphToGlyphArc(Arc arc) {
		if (arc.getTarget() instanceof Glyph && arc.getSource() instanceof Glyph) {
			return true;
		}
		return false;
	}
	
	public Boolean isPortToGlyphArc(Arc arc) {
		if (arc.getTarget() instanceof Port && arc.getSource() instanceof Glyph) {
			return true;
		}
		return false;		
	}
	
	public Boolean isGlyphToPortArc(Arc arc) {
		if (arc.getTarget() instanceof Glyph && arc.getSource() instanceof Port) {
			return true;
		}
		return false;		
	}
	
	// todo
	public Boolean isPortToPortArc(Arc arc) {
		if (arc.getTarget() instanceof Port && arc.getSource() instanceof Port) {
			return true;
		}		
		return false;
	}
	
	public Boolean isLogicArc(Arc arc) {
		String clazz = arc.getClazz();
		if (clazz.equals("logic arc")) {
			return true;
		} else if (clazz.equals("equivalence arc")) {
			return true;
		}
		return false;
	}
	
	public Boolean isModifierArc(String clazz) {
		if (clazz.equals("stimulation")) {
			return true;
		} if (clazz.equals("catalysis")) {
			return true;
		} if (clazz.equals("inhibition")) {
			return true;
		} if (clazz.equals("necessary stimulation")) {
			return true;
		} if (clazz.equals("modulation")) {
			return true;
		} else if (clazz.equals("unknown influence")) {
			return true;
		} // ...
		else {
			return false;
		} 
	}
	
	public Boolean isConsumptionArc(String clazz) {
		if (clazz.equals("consumption")) {
			return true;
		} // ...
		else {
			return false;
		} 
	}
	
	public Boolean isProductionArc(String clazz) {
		if (clazz.equals("production")) {
			return true;
		} else {
			return false;
		} 		
	}
	
		
	public SpeciesReferenceRole findReactionRole(String clazz) {
		SpeciesReferenceRole role = null;

		if (clazz.equals("consumption")) {
			role = SpeciesReferenceRole.SUBSTRATE;
		} else if (clazz.equals("production")) {
			role = SpeciesReferenceRole.PRODUCT;
		} else if (clazz.equals("consumption")) {
			role = SpeciesReferenceRole.SIDESUBSTRATE;
		} else if (clazz.equals("production")) {
			role = SpeciesReferenceRole.SIDEPRODUCT;
		} else if (clazz.equals("catalysis")) {
			role = SpeciesReferenceRole.ACTIVATOR;
		} else if (clazz.equals("inhibition")) {
			role = SpeciesReferenceRole.INHIBITOR;
		} else if (clazz.equals("modulation")) {
			role = SpeciesReferenceRole.MODIFIER;	// not sure
		} else if (clazz.equals("unknown influence")) {
			role = SpeciesReferenceRole.UNDEFINED;
		}
		
		return role;		
	}
		
	public void addAnnotation(SBase species, String clazz, Qualifier qualifier) {
		Annotation annotation;
		CVTerm cvTerm;
		
		annotation = species.getAnnotation();
		// todo: use different namespace
		cvTerm = new CVTerm(Type.BIOLOGICAL_QUALIFIER, qualifier);
		// should be urn
		// add hasPart
		cvTerm.addResource(clazz);
		annotation.addCVTerm(cvTerm);
	}
	
	public void addSBO(Species species, String clazz) {
		int sboTerm = -1;
		
		if (clazz.equals("simple chemical")) {
			sboTerm = 247;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("macromolecule")) {
			sboTerm = 245;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("nucleic acid feature")) {
			sboTerm = 354;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("complex multimer")) {
			sboTerm = 418;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("complex")) {
			sboTerm = 253;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("macromolecule multimer")) {
			sboTerm = 420;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("nucleic acid feature multimer")) {
			sboTerm = 419;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("source and sink")) {
			sboTerm = 291;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("perturbing agent")) {
			sboTerm = 405;
			species.setSBOTerm(sboTerm);
		} else if (clazz.equals("unspecified entity")) {
			sboTerm = 285;
			species.setSBOTerm(sboTerm);			
		} else if (clazz.equals("simple chemical multimer")) {
			sboTerm = 421;
			species.setSBOTerm(sboTerm);
		}// ...
		
	}

	public static void debugSbgnObject(Map map){
		
		List<Arc> listOfArcs = map.getArc();
		List<Glyph> listOfGlyphs = map.getGlyph();
		// ...
		List<Port> listOfPorts;
		List<Glyph> listOfContainingGlyphs;
		
		String id;
		Glyph.State state;
		Glyph.Clone clone;
		//Glyph.Callout callout;
		Glyph.Entity entity;
		Label label;
		Bbox bbox;
		String clazz; 
		String orientation;
		Object compartmentRef;
		Float compartmentOrder;
		
		Arc.Start start;
		List<Arc.Next> next;
		Arc.End end;
		Object source;
		Object target;
		
		System.out.println("sbgnml2sbml.SBGNML2SBML_GSOC2017.debugSbgnObject: \n");
		for (Arc arc: listOfArcs) {
			id = arc.getId();
			listOfContainingGlyphs = arc.getGlyph();
			listOfPorts = arc.getPort();
			start = arc.getStart();
			next = arc.getNext();
			end = arc.getEnd();
			clazz = arc.getClazz();
			source = arc.getSource();
			target = arc.getTarget();
			
			System.out.format("arc id=%s clazz=%s start,end=%s \n"
					+ "next=%s source=%s target=%s \n "
					+ "source,target=%s \n \n",
					id, clazz, displayCoordinates(start, end),
					sizeOf(next, 3), isNull(source), isNull(target),
					displaySourceAndTarget(source, target));
		}
		for (Glyph glyph: listOfGlyphs) {
			id = glyph.getId();
			state = glyph.getState();
			clone = glyph.getClone();
			//callout = glyph.getCallout();
			entity = glyph.getEntity();
			label = glyph.getLabel();
			bbox = glyph.getBbox();
			listOfContainingGlyphs = glyph.getGlyph();
			listOfPorts = glyph.getPort();
			clazz = glyph.getClazz();
			orientation = glyph.getOrientation();
			compartmentRef = glyph.getCompartmentRef();
			compartmentOrder = glyph.getCompartmentOrder();
			
			System.out.format("glyph id=%s clazz=%s label=%s bbox=%s \n"
					+ "state=%s clone=%s callout=%s entity=%s \n"
					+ "listOfContainingGlyphs=%s listOfPorts=%s \n"
					+ "orientation=%s compartmentRef=%s compartmentOrder=%s \n \n",
					id, clazz, isNull(label), displayCoordinates(bbox), 
					isNull(state), isNull(clone), "isNull(callout)", isNull(entity),  
					sizeOf(listOfContainingGlyphs, 0), sizeOf(listOfPorts, 2), 
					orientation, isNull(compartmentRef), isNull(compartmentOrder));
			
		}
	}
	
	public static String displaySourceAndTarget(Object source, Object target) {

		String type = "";
		if (source instanceof Glyph && target instanceof Port ) {
			type = "inward";
		}
		if (source instanceof Port && target instanceof Glyph ) {
			type = "outward";
		}	
		if (source instanceof Glyph && target instanceof Glyph ) {
			type = "undirected";
		}		
		
		return String.format("%s %s %s", source.getClass(), target.getClass(), type);		
	}
	
	public static String displayCoordinates(Bbox bbox) {
		if (bbox == null) {
			return "[ ]";
		}
		String X = Float.toString(bbox.getX());
		String Y = Float.toString(bbox.getY());
		String W = Float.toString(bbox.getW());
		String H = Float.toString(bbox.getH());
			
		return String.format("[x=%s y=%s w=%s h=%s]", X, Y, W, H);
	}

	public static String displayCoordinates(Arc.Start start, Arc.End end) {
		List<org.sbgn.bindings.Point> listOfPoints;
		int numOfPoints;
		
		if (start == null || end == null) {
			return "[ ]";
		}
		String startX = Float.toString(start.getX());
		String startY = Float.toString(start.getY());
		listOfPoints = end.getPoint();
		numOfPoints = listOfPoints.size();		
		String endX = Float.toString(end.getX());
		String endY = Float.toString(end.getY());
		
		return String.format("[(%s,%s) %s (%s,%s)]", startX, startY, Integer.toString(numOfPoints), endX, endY);
	}
	
	public static String isNull(Object o) {
		if  (o == null) {
			return "NULL";
		}
		return "NOT NULL";
	}
	
	public static String sizeOf(Object list, int elementType) {		
		if (list != null) {
			if (elementType == 0) {
				List<Glyph> listOfGlyphs = (List<Glyph>) list;
				return Integer.toString(listOfGlyphs.size());
			} else if (elementType == 2) {
				List<Port> listOfPorts = (List<Port>) list;
				return Integer.toString(listOfPorts.size());
			} else if (elementType == 3) {
				List<Arc.Next> listOfNext = (List<Arc.Next>) list;
				int numOfPoints = 0;
				List<org.sbgn.bindings.Point> listOfPoints;
				for (Arc.Next next : listOfNext) {
					listOfPoints = next.getPoint();
					numOfPoints += listOfPoints.size();
				}
				return String.format("%s/%s", Integer.toString(listOfNext.size()), Integer.toString(numOfPoints));
			}			
		}
			
		return "0";
	}
	
	public void printHelper(String source, String message){
		if (debugMode == 1){
			System.out.println("[" + source + "] " + message);
		}
	}	
	
	public void printHelper(String source, Integer message){
		if (debugMode == 1){
			System.out.println("[" + source + "] " + Integer.toString(message));
		}
	}	
	
//	public void displayReactionGlyphInfo(SWrapperModel sWrapperModel) {
//		for (String key : sWrapperModel.listOfWrapperReactionGlyphs.keySet()){
//			SWrapperReactionGlyph sWrapper = sWrapperModel.getWrapperReactionGlyph(key);
//			debugMode = 1;
//			printHelper(sWrapper.reactionId+"-inward", sWrapper.glyphToPortArcs.size());
//			printHelper(sWrapper.reactionId+"-outward", sWrapper.portToGlyphArcs.size());
//			printHelper(sWrapper.reactionId+"-undirected", sWrapper.glyphToGlyphArcs.size());
//			debugMode = 0;
//		}
//	}
	
	
	public static Sbgn readSbgnFile(String sbgnFileNameInput) {
		Sbgn sbgnObject = null;
		File inputFile;
		
		inputFile = new File(sbgnFileNameInput);
		try {
			sbgnObject = SbgnUtil.readFromFile(inputFile);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return sbgnObject;
	}
	
	public static void writeSbmlFile(String sbmlFileNameOutput, Model model) {
		File outputFile;
		outputFile = new File(sbmlFileNameOutput);
		
		SBMLWriter sbmlWriter;
		SBMLDocument sbmlDocument;
		
		sbmlWriter = new SBMLWriter();
		sbmlDocument = new SBMLDocument(3, 1);
		sbmlDocument.setModel(model);
		
		try {
			sbmlWriter.writeSBML(sbmlDocument, outputFile);
		} catch (SBMLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

		
}
