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

The diagrams above were generated using [draw.io](https://www.draw.io/#). The diagrams were stored in XML format in [documentation](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/tree/master/documentation). These can be imported and edited.
### Note 1:
For the converter `SBML 'core' <- SBGN PD` and `SBML 'layout' <- SBGN PD`, we have multiple ways to map SBGN 'Logic arc', 'Equivalence arc', 'And', 'Or', 'Not' to SBML 'layout' objects. There are 3 alternatives we have discussed in [Post #10](http://haleyyew-gsoc.blogspot.ca/2017/07/week-10.html) in the blog and in an [Github Issue](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/issues/51). The example that we use is [or-simple.sbgn](https://github.com/sbgn/libsbgn/blob/master/test-files/PD/or-simple.png).

1. Alternative 1 (incorrect)
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/documentation/or-simple_mapping1.png)

However, we discussed in the blog that this violates the syntax of SBML 'core' and 'layout'. A ModifierSpeciesReference(MSR) cannot participate in 2 reactions. In this case, if the 'OR' glyph is mapped to a Reaction, then MSR would be a Product in the 'OR' Reaction. But then MSR is at the same time a Modifier in the 'Subst->Prod' Reaction.

2. Alternative 2 (not optimal)
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/documentation/or-simple_mapping2.png)

If we map the 'OR' glyph to a GeneralGlyph, then we lose many objects in the SBML 'core' model. The 'core' model no longer stores the 2 Logic arcs, the 'OR' glyph, and the Modifier arc. 

3. Alternative 3
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/documentation/or-simple_mapping3.png)

We end up using this option because it allows us to store as many objects in the 'core' model as possible. There is a tradeoff: since we map the 'OR' glyph to a Species+SpeciesGlyph, we cannot draw the center Curve that connects the Arcs. We need to create a separate ReferenceGlyph to mimic the appearance of the center Curve of a ReactionGlyph.

We also lose the 2 Logic arcs in the 'core' model, but we managed to insert an Annotation element for the 2 mapped ReferenceGlyphs that informs to whom they interact with. For example, in one of the ReferenceGlyphs, the Annotation indicates the following:
```
	<bqbiol:hasProperty>
	<rdf:Bag>
	<rdf:li rdf:resource="glyph2"/>
	<rdf:li rdf:resource="glyph3"/>
	</rdf:Bag>
	</bqbiol:hasProperty>
```
The ReferenceGlyph's 'source' is Species="glyph2" (which is Enzym2), and the 'target' is Species="glyph3" (which is 'OR').

The converted SBML model for or-simple.sbgn is [or-simple_SBML.xml](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_pd_examples_converted/or-simple_SBML.xml).

### Note 2:
For the converter `SBML 'core' -> SBGN PD`, we were concerned with not being able to store any 'layout' information contained inside an Event object in SBML 'core'. The approach that we plan to use, in the case where an Event changes the state of Species and Reactions, is to store multiple diagrams for different time points. An example is given below. This example was taken from [7.11 Example involving events](http://sbml.org/Special/specifications/sbml-level-3/version-1/core/release-2/sbml-level-3-version-1-core-release-2.pdf) in the SBML 'core' specification (with slight modifications).
![](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/documentation/note2.png)
Consider a system with two genes, G1 and G2. G1 is initially on and G2 is initially off. When G1 is turned on, the two genes, together, lead to the production of two products, P1 and P2. When P1 reaches a given concentration (by some other reactions), G2 switches on.

This idea has not been tested due to the lack of examples and a lack of time.

### Note 3:
For the converter `SBML 'core' <- SBGN AF`, we are concerned with how to map SBGN 'Logic arc', 'Equivalence arc', 'And', 'Or', 'Not' to SBML 'layout' objects to SBML KineticLaw. We found that KineticLaw might be a suitable candidate, however there will not be a Reaction in the 'core' model. For example, consider the SBGN model [compartment_extended.sbgn](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_af_examples/compartment_extended.sbgn):
![compartment_extended.sbgn](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_af_examples/compartment_extended.png)
The KineticLaw that describes the Logic circuit network from the "co-repressor", "co-activator", and the "RNA polymerase" to the "Gene transcription" is the following:
```
          <kineticLaw>
            <math xmlns="http://www.w3.org/1998/Math/MathML">            
              <apply>
                <or/>
                <apply>
                  <and/>
                  <ci> g2 </ci>
                  <ci> g3 </ci>
                </apply>
                <ci> g6 </ci>
              </apply>
            </math>
          </kineticLaw>
```
The initial values of g2, g3, and g6 must be either True or False. This idea was not implemented due to the lack of time, however, the idea in Note 4 was implemented. 

### Note 4:
For the converter `SBML 'qual' <-> SBGN AF`, we mapped SBGN 'Logic arc', 'Equivalence arc', 'And', 'Or', 'Not' to SBML 'qual' Transition, which stores a FunctionTerm. The example we worked with was [compartment_extended.sbgn](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_af_examples/compartment_extended.sbgn), it was converted using [SBGNML2SBMLQual.java](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/code/src/org/sbfc/converter/sbgnml2sbml/qual/SBGNML2SBMLQual.java). The converted SBML 'qual' model was [compartment_extended_SBML+qual.xml](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/blob/master/sbgnml_af_examples_converted/compartment_extended_SBML%2Bqual.xml). Notice how the Logic circuit network was mapped to the following Transition:
```
      <qual:transition qual:id="g7">
        <qual:listOfInputs>
          <qual:input qual:id="Input_g2_in_g7" qual:qualitativeSpecies="g2" qual:transitionEffect="none"/>
          <qual:input qual:id="Input_g3_in_g7" qual:qualitativeSpecies="g3" qual:transitionEffect="none"/>
          <qual:input qual:id="Input_g6_in_g7" qual:qualitativeSpecies="g6" qual:transitionEffect="none"/>
        </qual:listOfInputs>
        <qual:listOfOutputs>
          <qual:output qual:id="Output_g5_in_g7" qual:qualitativeSpecies="g5" qual:transitionEffect="assignmentLevel"/>
        </qual:listOfOutputs>
        <qual:listOfFunctionTerms>
          <qual:defaultTerm qual:resultLevel="1">
          </qual:defaultTerm>
          <qual:functionTerm qual:resultLevel="0">
            <math xmlns="http://www.w3.org/1998/Math/MathML">            
              <apply>
                <or/>
                <apply>
                  <and/>
                  <ci> Input_g2_in_g7 </ci>
                  <ci> Input_g3_in_g7 </ci>
                </apply>
                <ci> Input_g6_in_g7 </ci>
              </apply>
            </math>
          </qual:functionTerm>
        </qual:listOfFunctionTerms>
      </qual:transition>
```

### Note 5:
Since SBGN and SBML are not semantically equivalent, we cannot perform a one-to-one mapping between SBGN objects and SBML objects. However, we aim to preserve the information in some way so that it can be restored once we perform a roundtrip conversion. That is, we aim to preserve:
1. information in SBML 'core' that cannot be mapped directly to SBGN objects, and 
2. information in SBGN objects that cannot be mapped directly to SBML 'core'. 

#### 1. 
For example, as we discussed in [Post #14](http://haleyyew-gsoc.blogspot.ca/2017/08/week-14.html) in the blog, if we have the following listOfUnitDefinitions in SBML,
```
    <listOfUnitDefinitions>
      <unitDefinition id="per_second">
        <listOfUnits>
          <unit exponent="-1" kind="second" />
        </listOfUnits>
      </unitDefinition>
      <unitDefinition id="litre_per_mole_per_second">
        <listOfUnits>
          <unit exponent="-1" kind="mole" />
          <unit exponent="1" kind="litre" />
          <unit exponent="-1" kind="second" />
        </listOfUnits>
      </unitDefinition>
</listOfUnitDefinitions>
```
The UnitDefinitions will be preserved as SBGN Extension elements like the following:
```
    <extension>
        <sbfcanno:unitDefinition xmlns:sbfcanno="http://www.sbfc.org/sbfcanno" sbfcanno:name="">
            <sbfcanno:kindSECOND sbfcanno:exponent="-1.0" sbfcanno:kind="SECOND" sbfcanno:multiplier="1.0" sbfcanno:scale="0"/>
        </sbfcanno:unitDefinition>
        <sbfcanno:unitDefinition xmlns:sbfcanno="http://www.sbfc.org/sbfcanno" sbfcanno:name="">
            <sbfcanno:kindMOLE sbfcanno:exponent="-1.0" sbfcanno:kind="MOLE" sbfcanno:multiplier="1.0" sbfcanno:scale="0"/>
            <sbfcanno:kindLITRE sbfcanno:exponent="1.0" sbfcanno:kind="LITRE" sbfcanno:multiplier="1.0" sbfcanno:scale="0"/>
            <sbfcanno:kindSECOND sbfcanno:exponent="-1.0" sbfcanno:kind="SECOND" sbfcanno:multiplier="1.0" sbfcanno:scale="0"/>
        </sbfcanno:unitDefinition>
</extension>
```
#### 2.
In an SBGN Arc, the we cannot directly map the Arc.source and Arc.target attributes. However, we can preserve the values by using the Miriam Qualifiers. For example, 
```
<arc target="glyph23.1" source="glyph13" id="arc18" class="logic arc">...</arc>
```
The source and target glyph id would be stored in a CVTerm in the Annotation element.
```
        <annotation>
	<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:bqbiol="http://biomodels.net/biology-qualifiers/">
	<rdf:Description rdf:about="#e9d8584d-c080-43fd-99d2-b2f2233186a7">
	<bqbiol:hasProperty>
	<rdf:Bag>
	<rdf:li rdf:resource="glyph13"/>
	<rdf:li rdf:resource="glyph23"/>
	</rdf:Bag>
	</bqbiol:hasProperty>
	</rdf:Description>
	</rdf:RDF>
	</annotation>
```
However, using the Miriam Qualifiers could cause a conflict with an existing Annotation that already uses the Qualifiers. A custom annotation should be created to store these preserved information, but this was not implemented. So far, the following Biology Qualifiers were used:
1. `Qualifier.BQB_IS_VERSION_OF` to store the Glyph.clazz or Arc.clazz attribute
2. `Qualifier.BQB_HAS_PROPERTY` to store the Arc.source and Arc.target attributes
3. `Qualifier.BQB_IS_PART_OF` to store the parent-child relationship between glyphs. Consider a complex containing many macromolecules. Then a macromolecule is part of the complex.

## Wrappers for mapping

### Mapping "SBML Layout glyphs" to "SBGN ML Glyphs" using Wrappers
The wrappers were created to store meta information for an object in SBGN or in SBML. For example, in SBGN, a SWrapperArc was created for an Arc. The SWrapperArc stores the Arc, and because we use the Arc.source and Arc.target attributes in the SBGN->SBML conversion, the Object referenced by Arc.source and Arc.target need to be known. However we do not know the identity of the objects directly from Arc.source and Arc.target, we need to do some computations to find out the identities. We store the identities in SWrapperArc, so that we do not need to recompute the identities in the future, and we can retrieve the values easily from SWrapperArc. Most of the wrappers below are not as useful as expected, we hope that they may become more useful in the future as the converter gets more complex.

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
### SBML->SBGN
1. Most of the mathematical formula of the SBML model are ignored, only the reaction layout is converted.
2. FunctionDefinition,UnitDefinition,Parameters,InitialAssignment,Rule,Constraint,Event
3. Attributes of Species, Reaction, Compartment, SpeciesReference, and ModifierSpeciesReference
### SBGN->SBML
1. The source and target reference of an Arc 
2. compartmentRef of a Glyph

## Problems
The problems listed here also have a TODO task in the converter code. Please refer to the TODO tasks for detailed information.
### Problem 1: SBML layout <- SBGN PD
See Note 1 in [Mapping diagrams](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/wiki/Mapping-diagrams)

### Problem 2: SBML layout+render <- SBGN PD
SBGN Clone markers cannot display text. See the 'labeledCloneMarker.sbgn' example in [Post #12](http://haleyyew-gsoc.blogspot.ca/2017/07/week-12.html) in the blog.

### Problem 3: SBML layout+render <- SBGN PD
Does not consider the Glyph.orientation of the SBGN glyphs when creating 'layout', therefore Points that are not specified explicitly may get misplaced. See SBGNML2SBMLRender.findObjectRole for details.

### Problem 4: SBML layout <- SBGN PD
We assume in SBGNML, Arc.start=Arc.source, Arc.end=Arc.target, but this might not be true. See SBGNML2SBML_GSOC2017.createOneModifierSpeciesReferenceGlyph for details.

### Problem 5: SBML layout <- SBGN PD
Assume in SBML, ReferenceGlyph.glyph is SpeciesGlyph, ReferenceGlyph.reference is a ReactionGlyph, see SBGNML2SBML_GSOC2017.createOneReferenceGlyph for details.

### Problem 6: SBML layout+render <- SBGN PD
Ordering of CompartmentGlyphs is incorrect if rendered using the 'render' package. See [Post #12](http://haleyyew-gsoc.blogspot.ca/2017/07/week-12.html) in the blog and [GitHub Issue](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/issues/47) for details.

### Problem 7: SBML 'core' -> SBGN PD
See Note 2 in [Mapping diagrams](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/wiki/Mapping-diagrams)

### Problem 8: SBML 'core' <- SBGN AF
See Note 3 in [Mapping diagrams](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/wiki/Mapping-diagrams)

### Problem 9: SBML 'qual' <-> SBGN AF
See Note 4 in [Mapping diagrams](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/wiki/Mapping-diagrams)

### Problem 10: Preserving information in models
See Note 5 in [Mapping diagrams](https://github.com/NRNB-GSoC2017-SBML2SBGNML-Converters/SBML2SBGNML/wiki/Mapping-diagrams)

### Problem 11:
When converting Species, all Species are placed in the DefaultCompartment. Need to check compartmentRef of the Sbgn glyph to find out which SBML compartment to contain the Species. See SBGNML2SBMLUtil.createDefaultCompartment for details.

### Problem 12:
Cannot find the correct 'clazz' for the Arc if we convert from SBML to SBGN AF. This is because there is only one type of Modulation in SBML, but there are many more 'clazz' for Modulation arcs in SBGN. See SBML2SBGNMLUtil.searchForReactionRole and SBML2SBGNML_GSOC2017.mapObjectRoleToClazz for details.

### Problem 13:
The splitting pattern to parse the id of an SBGN object is not correct, need a better splitting pattern. See SBML2SBGNML_GSOC2017.checkParentChildGlyph for details.

### Problem 14:
The architecture of SWrapperGlyphEntityPool needs to be changed. See SBML2SBGNML_GSOC2017.addCloneMarkers for details.

### Problem 15: Rendering Styles for SBML
See the problems in [Post #15](http://haleyyew-gsoc.blogspot.ca/2017/08/week-15.html) for details.
