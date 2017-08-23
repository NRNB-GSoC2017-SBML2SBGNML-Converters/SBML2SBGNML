# SBML-SBGNML Converters
Welcome to the SBML2SBGNML wiki!

## What is SBML2SBGNML?
SBML2SBGNML is a converter between two XML-based file formats: SBML and SBGNML. SBML2SBGNML converts SBML files to SBGNML and vice versa. Both SBML and SBGNML are used by systems biology researchers to store graph-based diagrams (i.e. diagrams that consists of nodes and edges). The diagrams are generated as part of a research publication that serve as a simplification of the underlying models in order to communicate with non-experts, however, the main research results are the detailed models (containing formulas such as reaction rates of interacting species). For more information, please see the list of readings in [Post #1](http://haleyyew-gsoc.blogspot.ca/2017/05/greetings-to-google-summer-of-code.html) of the project's blog.

SBML is able to store the detailed models in its 'core' package, while storing the diagram in its 'layout' extension package and enhancing the graphics of the diagram in its 'render' extension package. SBGNML (PD, AF, and ER) stores the diagram only, and has a number of limitations including not being able to store colour for diagram objects. For more information, please see the list of documentations in [Post #2](http://haleyyew-gsoc.blogspot.ca/2017/05/initial-meeting-with-mentors.html) of the project's blog. Due to the richness of the information stored in SBML and the limitations in SBGNML, we can only transfer a limited amount of information between SBML and SBGNML. The following converters were implemented, each targeting to transfer a specific subset of information:

1. `SBML 'core' <-> SBGN PD`
2. `SBML 'layout'+'render' <-> SBGN PD`
3. `SBML 'core' <-> SBGN AF`
4. `SBML 'layout'+'render' <-> SBGN AF`
5. `SBML 'qual' <-> SBGN AF`

Where `<->` means bidirectional converter. For example, `SBML 'core' <-> SBGN PD` allows `SBML 'core'` to convert to `SBGN PD`, and `SBGN PD` to convert to `SBML 'core'`.

One practical use of these converters is to allow SBGNML diagrams to convert to SBML, so that SBML users only need work with SBML diagrams. Once the users made modifications to the SBML diagrams, they can convert the diagrams back to SBGNML that retains the changes made by the user. There are discussions of some possible uses on the [SBGN discussion forum](https://groups.google.com/forum/#!topic/sbgn-discuss/BzwhzEks2Wo/discussion). Note that not all of the 5 converters above allow diagram conversion. The results of the converted example diagrams by the converters are show below:

## Examples of converted diagrams
For a complete list of examples, please see 
[sbgnml_af_examples](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/sbgnml_af_examples), 
[sbgnml_pd_examples](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/sbgnml_pd_examples), [sbml_layout_examples](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/sbml_layout_examples),
[sbml_qual_examples](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/sbml_qual_examples),
[sbml_sbgnml_roundtrip_examples](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/sbml_sbgnml_roundtrip_examples) in the project's code repository. Some converted examples were displayed in [Post #16](http://haleyyew-gsoc.blogspot.ca/2017/08/week-16.html) of the project's blog. All SBML diagrams were generated using [COPASI](http://copasi.org/), and all SBGNML diagrams were generated using [SBML Layout Viewer](http://sysbioapps.dyndns.org/Layout).

### 2. `SBML 'layout'+'render' -> SBGN PD` converts from SBML to SBGN
Original SBML:
[e_coli_core_metabolism.sbml.xml](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbml_sbgnml_roundtrip_examples/e_coli_core_metabolism.sbml.xml)
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbml_sbgnml_roundtrip_examples/e_coli_core_metabolism.sbml.png)  

Converted SBGN:
[e_coli_core_metabolism.sbml_SBGN-ML.sbgn
](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbml_sbgnml_roundtrip_examples/e_coli_core_metabolism.sbml_SBGN-ML.sbgn)
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbml_sbgnml_roundtrip_examples/e_coli_core_metabolism.sbml_SBGN-ML.png)

#### Roundtrip conversion
Since we want to demonstrate the usability of the converter, we want to ensure that the converted information can be restored to the original information. This is the purpose of the roundtrip conversion. The roundtrip conversion involves a SBML -> SBGN conversion, and then SBGN -> SBML to convert back to the original file format. 

Roundtripped SBML:
[e_coli_core_metabolism.sbml_SBGN-ML_SBML.xml](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbml_sbgnml_roundtrip_examples/e_coli_core_metabolism.sbml_SBGN-ML_SBML.xml)
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbml_sbgnml_roundtrip_examples/e_coli_core_metabolism.sbml_SBGN-ML_SBML.jpg) 

### 2. `SBML 'layout'+'render' <- SBGN PD` converts from SBGN to SBML
Original SBGN:
[SBGN-PD_all.sbgn](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_pd_examples/SBGN-PD_all.sbgn)
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_pd_examples/SBGN-PD_all.png)

Converted SBML:
[SBGN-PD_all_SBML.xml](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_pd_examples_converted/SBGN-PD_all_SBML.xml)
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_pd_examples_converted/SBGN-PD_all_SBML.jpg)

#### Roundtrip conversion
Since we want to demonstrate the usability of the converter, we want to ensure that the converted information can be restored to the original information. This is the purpose of the roundtrip conversion. The roundtrip conversion involves a SBGN -> SBML conversion, and then SBML -> SBGN to convert back to the original file format. 

Roundtripped SBGN:
[SBGN-PD_all_SBML_SBGN-ML.sbgn](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_pd_examples_converted/SBGN-PD_all_SBML_SBGN-ML.sbgn)
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_pd_examples_converted/SBGN-PD_all_SBML_SBGN-ML.png)

### 4. `SBML 'layout'+'render' -> SBGN AF`
Original SBML:
[4.1 Simple Logical Regulatory Graph.xml](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbml_qual_examples/4.1%20Simple%20Logical%20Regulatory%20Graph.xml)
![](https://3.bp.blogspot.com/-IqOX7OBcRqY/WYF0cez8haI/AAAAAAAAHuI/hPCO6C4769cz3FNYv29Am2_LKAu1OjreQCLcBGAs/s1600/4.1%2BSimple%2BLogical%2BRegulatory%2BGraph.png)

Converted SBGN:
[4.1 Simple Logical Regulatory Graph_SBGN-ML.sbgn](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbml_qual_examples_converted/4.1%20Simple%20Logical%20Regulatory%20Graph_SBGN-ML.sbgn)
![](https://4.bp.blogspot.com/-tts-_kPk9nc/WYF0dKQrEhI/AAAAAAAAHuM/fZpO-z3PRnwM1hgHgXIbEcVYmfyXIJSMwCLcBGAs/s1600/4.1%2BSimple%2BLogical%2BRegulatory%2BGraph_SBGN-ML.png)

### 4. `SBML 'layout'+'render' <- SBGN AF` 
Original SBGN:
[compartment_extended.sbgn](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_af_examples/compartment_extended.sbgn)
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_af_examples/compartment_extended.png)

Converted SBML:
[compartment_extended_SBML.xml](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_af_examples_converted/compartment_extended_SBML.xml)
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_af_examples_converted/compartment_extended_SBML.jpg)

There is an alternative conversion `SBML 'qual' <- SBGN AF` for this example, please see the **Problems** section of this README.

## How to run the converters
1. First, `git clone https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML.git`  
2. Load the project in `code/` into an IDE such as Eclipse.  
3. Go to the [sbml2sbgnml](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/code/src/org/sbfc/converter/sbml2sbgnml) or the [sbgnml2sbml](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/code/src/org/sbfc/converter/sbgnml2sbml) directory. Choose the converter to run. 
    1. `SBML 'core' <-> SBGN PD`:
        1. For `SBML 'core' -> SBGN PD`, use [SBML2SBGNML_2011.java](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/code/src/org/sbfc/converter/sbml2sbgnml/SBML2SBGNML_2011.java)
        2. For `SBML 'core' <- SBGN PD`, use [SBML2SBGNML_GSOC2017.java](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/code/src/org/sbfc/converter/sbml2sbgnml/SBML2SBGNML_GSOC2017.java)
    2. `SBML 'layout'+'render' <-> SBGN PD`:
        1. For `SBML 'layout'+'render' -> SBGN PD`, use [SBML2SBGNML_GSOC2017.java](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/code/src/org/sbfc/converter/sbml2sbgnml/SBML2SBGNML_GSOC2017.java)
        2. For `SBML 'layout'+'render' <- SBGN PD`, use [SBGNML2SBML_GSOC2017.java](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/code/src/org/sbfc/converter/sbgnml2sbml/SBGNML2SBML_GSOC2017.java)
    3. `SBML 'core' <-> SBGN AF` is at the initial stage of implementation. Please see the **Problems** section of this README.
    4. `SBML 'layout'+'render' <-> SBGN AF`:
        1. For `SBML 'layout'+'render' -> SBGN AF`, use [SBML2SBGNML_GSOC2017.java](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/code/src/org/sbfc/converter/sbml2sbgnml/SBML2SBGNML_GSOC2017.java), set the `toLanguage` to "activity flow"
        2. For `SBML 'layout'+'render' <- SBGN AF`, use [SBGNML2SBML_GSOC2017.java](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/code/src/org/sbfc/converter/sbgnml2sbml/SBGNML2SBML_GSOC2017.java)
    5. `SBML 'qual' <-> SBGN AF`:
        1. For `SBML 'qual' -> SBGN AF`, use [SBML2SBGNML_GSOC2017.java](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/code/src/org/sbfc/converter/sbml2sbgnml/SBML2SBGNML_GSOC2017.java), set the `toLanguage` to "activity flow"
        2. For `SBML 'qual' <- SBGN AF`, use [SBGNML2SBMLQual.java](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/code/src/org/sbfc/converter/sbgnml2sbml/qual/SBGNML2SBMLQual.java)
4. In Eclipse, specify the input file path (relative path) in Run Configurations Arguments. For example, to run `SBML 'layout'+'render' <- SBGN PD`, put `"/../sbgnml_af_examples/compartment_extended.sbgn"` in Run Configurations Arguments.
5. Run the `main()` method.  
6. The converted file will be located in the same directory as the input file.

## Automated tests
Automated tests were added for `SBGNML2SBML_GSOC2017.java` and `SBML2SBGNML_GSOC2017.java` using JUnit. 
1. [TestConverter.java](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/code/test/org/sbfc/converter/sbgnml2sbml/TestConverter.java) tests some functions in `SBGNML2SBML_GSOC2017.java`, and 
2. [TestConverter.java](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/code/test/org/sbfc/converter/sbml2sbgnml/TestConverter.java) tests some functions in `SBML2SBGNML_GSOC2017.java`. 

Not all functions in the converters were tested. The purpose of the implemented test cases were to check of backward compatibility as the implementation of the converters get more complex (for example, initially, SBML2SBGNML converts examples with SBML 'layout' only. But later, it converts SBML 'layout'+'render'. We want to make sure SBML2SBGNML still can convert examples with SBML 'layout' only).

The files for unit test cases are located in [test-examples](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/code/test-examples).

The automated tests also convert all of the examples in [sbgnml_af_examples](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/sbgnml_af_examples), 
[sbgnml_pd_examples](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/sbgnml_pd_examples), [sbml_layout_examples](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/sbml_layout_examples),
[sbml_qual_examples](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/sbml_qual_examples),
[sbml_sbgnml_roundtrip_examples](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/sbml_sbgnml_roundtrip_examples) in the project's repository. The project's repository already contains all the examples converted, the converted files can be found in [examples_directory_name]_converted (such as [sbgnml_pd_examples_converted](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/sbgnml_pd_examples_converted)).

## Mechanism of converters
Before reading the code in the converters, please briefly read the descriptions of how the converters were implemented, below.
### SBML2SBGNML_GSOC2017.java
The converter first converts all the SBML 'layout' CompartmentGlyphs to SBGN 'Encapsulation' glyphs; then SpeciesGlyphs to 'Entity pool node' or 'Logical operator' glyphs; then GeneralGlyphs to 'Auxiliary Units' glyphs and ReferenceGlyphs to 'Logic/Equivalence/Modulation arc'; the ReactionGlyphs to 'Entity pool node' glyphs and SpeciesReferenceGlyphs to 'Arcs' (excluding 'Logic/Equivalence arc'). Note that a ReactionGlyph with its SpeciesReferenceGlyphs are converted into an Arcgroup. 

We add the SBGN glyph/arc to the Sbgn.Map as soon as we finish converting each 'layout' object, except when the converted SBGN glyph is a "child" of another glyph (for example, a 'Unit of Information' is a child of another glyph). Then, `addChildGlyphsToParent()` will add all the converted SBGN glyphs that are "child"s to Sbgn.Map. Note that as we convert from SpeciesReferenceGlyphs to 'Arcs', we fill in the Arc.source and Arc.target attributes (because the values are easy to figure out). But as we convert from ReferenceGlyphs to 'Arcs', we cannot figure out Arc.source and Arc.target attributes easily, this is why we need to call a separate `addPortForArc()` function is figure out the values (using Annotations). 

Then we call `createLabelsFromTextGlyphs()` to convert TextGlyphs to Labels for a specific SBGN glyph. Lastly, we add the Glyph.Clone markers to all the SBGN glyphs that should contain them.

### SBGNML2SBML_GSOC2017.java
First, we classify all the SBGN Glyph, Arcs, and ArcGroups into different lists: entityPoolNodes, processNodes, compartments, logicOperators, consumptionArcs, productionArcs, modifierArcs, logicArcs. Then we iterate each list to create one of the following SBML combinations: Species+SpeciesGlyph, Reaction+ReactionGlyph, Compartment+CompartmentGlyph, GeneralGlyph, ReferenceGlyph, SpeciesReference+SpeciesReferenceGlyph, ModifierSpeciesReference+SpeciesReferenceGlyph. Lastly, we render all the converted 'layout' objects by assigning an 'renderObject' id to each object. Each id refers to a Style in LocalRenderInformation. All the Styles were pre-loaded from a template file located in [template.xml](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/code/test-examples/sbgnml_unittest/template.xml).

### SBGNML2SBMLQual.java
The conversion is similar to SBGNML2SBML_GSOC2017.java. Since we are creating SBML 'qual' objects, we convert to QualitativeSpecies instead of Species, and we convert to Transitions instead of Reactions. The mechanism of `createTransitions` and `createCompleteTransitions()` are significantly different, please read the comments in the code to understand the mechanism.

## Mapping diagrams
We have 5 bidirectional converters, we show how the model objects are mapped between SBGN and SBML. 
1. `SBML 'core' <-> SBGN PD`
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/documentation/SBML2SBGN_%5Bcore_to_PD%5D_MIRIAM%2BSBO.png)
2. `SBML 'layout'+'render' <-> SBGN PD`
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/documentation/SBML2SBGN_%5Blayout_to_PD%5D_MIRIAM%2BSBO.png)
3. `SBML 'core' <-> SBGN AF`
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/documentation/SBML2SBGN_%5Bcore_to_AF%5D_MIRIAM%2BSBO.png)
4. `SBML 'layout'+'render' <-> SBGN AF` \
The mapping diagram is almost identical to `SBML 'layout'+'render' <-> SBGN PD`
5. `SBML 'qual' <-> SBGN AF`
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/documentation/SBML2SBGN_%5Bqual_to_AF%5D_MIRIAM%2BSBO.png)

Note 1:
SBML core <-> SBGN PD
Logic arc 
Equivalence arc
And
Or
Not

Note 2:
SBML core <-> SBGN PD
Event
Multiple diagrams for different time points

Note 3:
SBML core <-> SBGN AF
KineticLaw

Note 4:
SBML qual <-> SBGN AF
Transition stores FunctionTerm

Note 5:
Preserving information in SBML core

## Wrappers for mapping

### Mapping "SBML Layout glyphs" to "SBGN ML Glyphs" using Wrappers

#### 1.1 SWrapperMap
```
public class SWrapperMap {
	Map map;
	Model model;
	
	HashMap<String, SpeciesGlyph> listOfWrapperSpeciesGlyphs;
	HashMap<String, CompartmentGlyph> listOfWrapperCompartmentGlyphs;
	HashMap<String, ReactionGlyph> listOfWrapperReactionGlyphs;
	HashMap<String, SpeciesReferenceGlyph> listOfWrapperSpeciesReferenceGlyphs;
	HashMap<String, GeneralGlyph> listOfWrapperGeneralGlyphs;
	HashMap<String, ReferenceGlyph> listOfWrapperReferenceGlyphs;
	HashMap<String, TextGlyph> listOfTextGlyphs;
	
	HashMap<String, SWrapperArc> listOfSWrapperArcs;
	HashMap<String, SWrapperArcGroup> listOfSWrapperArcGroups;
	HashMap<String, SWrapperGlyphEncapsulation> listOfSWrapperGlyphEncapsulations;
	HashMap<String, SWrapperGlyphEntityPool> listOfSWrapperGlyphEntityPools;
	HashMap<String, SWrapperGlyphProcess> listOfSWrapperGlyphProcesses;
	HashMap<String, SWrapperGlyphLogicOperator> listOfSWrapperGlyphLogicOperators;

	HashMap<String, String> notAdded
}
```
Mapping SBML `Model`+'layout'->SBGN `Map` \
`Model` contains `Species`, `SpeciesGlyph`, `Compartment`, `CompartmentGlyph`, etc. \
`Map` contains `Glyph`s: Process Nodes, Entity Pool Nodes, Encapsulations, etc. \
`Map` contains `Arc`s and `ArcGroup`s etc. 
`notAdded` contains all the glyphs that are nested inside parent glyphs

#### 1.2 SWrapperGlyphEncapsulation 
```
public class SWrapperGlyphEncapsulation {
	Compartment compartment;
	CompartmentGlyph compartmentGlyph;
	Glyph glyph;
}
```
Mapping SBML `Compartment`+`CompartmentGlyph`->SBGN `Glyph`

#### 1.3 SWrapperGlyphEntityPool 
```
public class SWrapperGlyphEntityPool {
	Species species;
	SpeciesGlyph speciesGlyph;
	TextGlyph textGlyph;
	String clazz;
	
	Glyph glyph;
	HashMap<String, Glyph> glyphs;
}
```
Mapping SBML `Species`+`SpeciesGlyph`+`TextGlyph`->SBGN Entity Pool `Glyph` \
`glyphs` stores nested `Glyph`s that are contained inside

#### 1.4 SWrapperGlyphProcess 
```
public class SWrapperGlyphProcess {
	ReactionGlyph reactionGlyph;
	Reaction reaction;
	TextGlyph textGlyph;
	
	HashMap<String, SpeciesReferenceGlyph> speciesReferenceGlyphs;
	HashMap<String, ReferenceGlyph> referenceGlyphs;
	HashMap<String, SpeciesReference> speciesReferences;
	HashMap<String, ModifierSpeciesReference> modifierSpeciesReferences;
	
	HashMap<String, SWrapperArc> arcs;
}
```
Mapping SBML `Reaction`+`ReactionGlyph`+`TextGlyph`->SBGN Process `Glyph` \
`arcs` stores `Arc`s that has either Source or Target pointing to the `Glyph`, \
note that this extra information is not part of the Process `Glyph` \ 
Mapping of SBML->SBGN works as follows: \
(case 1) `SpeciesReferenceGlyph`+`SpeciesReference`->`Arc` \
(case 2) `SpeciesReferenceGlyph`+`ModifierSpeciesReference`->`Arc` \
(case 3) `ReferenceGlyph`->`Arc`

#### 1.5 SWrapperLogicOperator 
```
public class SWrapperLogicOperator {
	SpeciesGlyph speciesGlyph;
	Species species;
	TextGlyph textGlyph;
	
	HashMap<String, ReferenceGlyph> referenceGlyphs;
	
	HashMap<String, SWrapperArc> logicArcs;
}
```
Mapping SBML `Species`+`SpeciesGlyph`+`TextGlyph`->SBGN Logic Operator`Glyph` \
`logicArcs` stores `Arc`s that has either Source or Target pointing to the `Glyph`, note that this extra information is not part of the Logic Operator `Glyph` \
Mapping of SBML->SBGN works as follows: \
`ReferenceGlyph`->`Arc`

#### 1.6 SWrapperArc 
```
public class SWrapperArc {
	boolean isSpeciesReferenceGlyph;
	SpeciesReferenceGlyph speciesReferenceGlyph;
	ReferenceGlyph referenceGlyph;
	
	boolean hasSpeciesReference;
	SpeciesReference speciesReference;
	ModifierSpeciesReference modifierSpeciesReference;
	
	Arc arc;
}
```
Mapping of SBML->SBGN works as follows: \
(case 1) `SpeciesReferenceGlyph`+`SpeciesReference`->`Arc` \
(case 2) `SpeciesReferenceGlyph`+`ModifierSpeciesReference`->`Arc` \
(case 3) `ReferenceGlyph`->`Arc`

#### 1.7 SWrapperArcGroup 
```
public class SWrapperArcGroup {
	boolean isReactionGlyph;
	ReactionGlyph reactionGlyph;
	GeneralGlyph generalGlyph;
	Reaction reaction;
	
	HashMap<String, SpeciesReferenceGlyph> speciesReferenceGlyphs;
	HashMap<String, ReferenceGlyph> referenceGlyphs;
	HashMap<String, SpeciesReference> speciesReferences;
	HashMap<String, ModifierSpeciesReference> modifierSpeciesReferences;
	HashMap<String, TextGlyph> textGlyph; 
	HashMap<String, SpeciesGlyph> speciesGlyphs;
	
	Arcgroup arcGroup;
	
	HashMap<String, SWrapperArc> arcs;
	HashMap<String, Glyph> glyphs;
}
```
`Arcgroup` consists of a collection of `Arc`s and `Glyph`s 
Mapping of one SBML->SBGN element works as follows: \
(case 1) \
`ReactionGlyph`+`Reaction`+`TextGlyph`->`Glyph` \
`SpeciesReferenceGlyph`+`SpeciesReference`->`Arc` \
(case 2) \
`ReactionGlyph`+`Reaction`+`TextGlyph`->`Glyph` \
`SpeciesReferenceGlyph`+`ModifierSpeciesReference`->`Arc` \
(case 3) \
`GeneralGlyph`+`TextGlyph`->`Glyph` \
`ReferenceGlyph`->`Arc` \
(case 1,2,3) A `GeneralGlyph` might store subglyphs, they will be stored in `speciesGlyphs`

### Mapping "SBGN ML Glyphs" to "SBML Layout glyphs" using Wrappers

#### 1.1 SWrapperModel 
```
public class SWrapperModel {
	HashMap<String, Glyph> processNodes;
	HashMap<String, Glyph> entityPoolNodes;
	HashMap<String, Glyph> compartments;
	HashMap<String, Glyph> logicOperators;
	
	HashMap<String, SWrapperArc> consumptionArcs;
	HashMap<String, SWrapperArc> productionArcs;
	HashMap<String, SWrapperArc> logicArcs;
	HashMap<String, SWrapperArc> modifierArcs;   

	HashMap<String, SWrapperSpeciesGlyph> listOfWrapperSpeciesGlyphs;
	HashMap<String, SWrapperCompartmentGlyph> listOfWrapperCompartmentGlyphs;
	HashMap<String, SWrapperReactionGlyph> listOfWrapperReactionGlyphs;
	HashMap<String, SWrapperSpeciesReferenceGlyph> listOfWrapperSpeciesReferenceGlyphs;
	HashMap<String, SWrapperGeneralGlyph> listOfWrapperGeneralGlyphs;
	HashMap<String, SWrapperReferenceGlyph> listOfWrapperReferenceGlyphs;
	HashMap<String, SWrapperQualitativeSpecies> listOfSWrapperQualitativeSpecies;
	HashMap<String, SWrapperTransition> listOfSWrapperTransitions;

	HashMap<String, String> portGlyphMap = new HashMap<String, String>(); 
}
```
Mapping SBGN `Map`->SBML `Model`+'layout'+'render' \
`Map` contains `Glyph`s: `processNodes`, `entityPoolNodes`, etc. \
`Map` contains `Arc`s: `consumptionArcs`, `productionArcs`, etc. \
`Model` contains `Species`, `SpeciesGlyph`, `Compartment`, `CompartmentGlyph`, etc. \
`portGlyphMap` stores the mapping from a `Port` id to a `Glyph` id that the `Port` belongs to.

#### 1.2 SWrapperArc 
```
public class SWrapperArc {
	Arc arc;
	String arcId;
	String sourceTargetType;
	String arcClazz;
	String sourceId;
	String targetId;
	Object source;
	Object target;
}
```
Stores extra meta information for an SBGN `Arc`. \
`sourceTargetType` tells whether the `Arc` Source is a `Glyph` or a `Port`, and whether the `Arc` Target is a `Glyph` or a `Port` \
`sourceId` is the `Glyph` id of the Source, even if the `Arc` points to a `Port`. \
`targetId` is the `Glyph` id of the Target, even if the `Arc` points to a `Port`.

#### 1.3 SWrapperCompartmentGlyph 
```
public class SWrapperCompartmentGlyph {
	Compartment compartment;
	CompartmentGlyph compartmentGlyph;
	Glyph glyph;
	String clazz;
```
Mapping SBGN `Glyph`->SBML `Compartment`+`CompartmentGlyph` 

#### 1.4 SWrapperSpeciesGlyph 
```
public class SWrapperSpeciesGlyph {
	Species species;
	SpeciesGlyph speciesGlyph;
	String clazz;
	Glyph sbgnGlyph;
	
	List<GraphicalObject> listOfGlyphs;
	TextGlyph textGlyph;

	boolean hasPort = false;
	boolean hasNestedGlyph = false;
	boolean hasAuxillaryUnits = false;
	boolean hasExtension = false;
	boolean hasLabel = false;
	String labelText = "";	
}
```
Mapping SBGN `Glyph`->SBML `Species`+`SpeciesGlyph`+`TextGlyph` \
`listOfGlyphs` stores all `Glyph`s that are nested inside, if the structure is recursive, the structure is flattened.

#### 1.5 SWrapperReactionGlyph 
```
public class SWrapperReactionGlyph {
	String reactionId;
	Reaction reaction;
	ReactionGlyph reactionGlyph;
	String clazz;
	Glyph glyph;
	
	HashMap<String, SWrapperArc> consumptionArcs;
	HashMap<String, SWrapperArc> productionArcs;
	HashMap<String, SWrapperArc> modifierArcs;

	HashMap<String, SpeciesReferenceGlyph> speciesReferenceGlyphs;

	List<Point> listOfEndPoints;
	
	SWrapperModel sWrapperModel;
}
```
Mapping SBGN `Glyph`->SBML `Reaction`+`ReactionGlyph` \
This class also stores `Arcs` and the mapped `SpeciesReferenceGlyph` that this `Reaction` is associated with.

#### 1.6 SWrapperSpeciesReferenceGlyph
```
public class SWrapperSpeciesReferenceGlyph{
	SpeciesReference speciesReference;
	SpeciesReferenceGlyph speciesReferenceGlyph;
	Arc arc;
	SWrapperArc sWrapperArc;
}
```
Mapping SBGN `Arc`->SBML `SpeciesReference`+`SpeciesReferenceGlyph` 

#### 1.7 SWrapperModifierSpeciesReferenceGlyph 
```
public class SWrapperModifierSpeciesReferenceGlyph extends SWrapperSpeciesReferenceGlyph {
	ModifierSpeciesReference speciesReference;
}
```
Mapping SBGN `Arc`->SBML `ModifierSpeciesReference`+`SpeciesReferenceGlyph` 

#### 1.8 SWrapperGeneralGlyph 
```
public class SWrapperGeneralGlyph {
	String clazz;
	GeneralGlyph generalGlyph;
	TextGlyph textGlyph;
	Glyph glyph;

	boolean hasParent;
	GraphicalObject parent;
		
	boolean glyphIsMissing;
	Arc arc;
	
	HashMap<String, Arc> arcs;
	HashMap<String, ReferenceGlyph> referenceGlyphs;
	
	SWrapperModel sWrapperModel;
}
```
(case 1) Mapping SBGN `Glyph`->SBML `GeneralGlyph`+`TextGlyph` or \
(case 2) Mapping SBGN `Arc`->SBML `GeneralGlyph` or \
(case 3) Mapping multiple SBGN `Glyph`s+`Arc`s->SBML `GeneralGlyph`+`TextGlyph` \
`arc` is the single `Arc` that maps to the `GeneralGlyph` (case 2) \
`arcs` stores all `Arc`s that maps to the `GeneralGlyph` (case 3) \
`referenceGlyphs` stores all `ReferenceGlyphs` the `GeneralGlyph` contains (case 1,2,3)

#### 1.9 SWrapperReferenceGlyph
```
public class SWrapperReferenceGlyph{
	Arc arc;
	ReferenceGlyph referenceGlyph;
	SWrapperArc sWrapperArc;
}
```
Mapping SBGN `Arc`->SBML `ReferenceGlyph`

## Information Lost:

## Problems
### Problem 1:
SBML layout <-> SBGN PD
See Note 1

### Problem 2:
SBGN Clone markers cannot display text.

### Problem 3:
Does not consider the Glyph.orientation of the SBGN glyphs. 

### Problem 4:
We assume in SBGNML, Arc.start=Arc.source, Arc.end=Arc.target, but this might not be true. See SBGNML2SBML_GSOC2017.createOneModifierSpeciesReferenceGlyph for details.

### Problem 5:
Assume in SBML, ReferenceGlyph.glyph is SpeciesGlyph, ReferenceGlyph.reference is a ReactionGlyph, see SBGNML2SBML_GSOC2017.createOneReferenceGlyph for details.

### Problem 6:
Ordering of compartments

### Problem 7:
See note 2

### Problem 8:
See note 3

### Problem 9:
See note 4

### Problem 10:
See note 5
