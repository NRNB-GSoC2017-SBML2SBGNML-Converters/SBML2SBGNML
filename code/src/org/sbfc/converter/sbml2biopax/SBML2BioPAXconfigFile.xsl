<?xml version="1.0" encoding="UTF-8"?>
<!-- XSL file for SBML2BioPAXconfigFile.xml -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="html" indent="yes" encoding="UTF-8" />
 <xsl:template match="/">
  <html>
   <!--head>
    <title>SBML2BioPAXconfigFile - A. Henry</title>
   </head-->
    <xsl:copy-of select="document('header.xml')" />
   <body style="font-family:helvetica;">

 <h1>BioPAXconfigFile</h1>
 <h2>publicationModel table:</h2>
  <xsl:apply-templates select="sbml2biopax/sbml2biopaxAnnotationParameter/publicationModel"/>

 <h2>speciesAnnotationMap table:</h2>
  <xsl:apply-templates select="sbml2biopax/sbml2biopaxAnnotationParameter/speciesAnnotationMap"/>

 <h2>taxonomyMap table:</h2>
  <xsl:apply-templates select="sbml2biopax/sbml2biopaxAnnotationParameter/taxonomyMap"/>
 
  <h2>annotationSpecies table:</h2>
  <p>databaseURI, <font color="red">constraint</font></p>
  <xsl:apply-templates select="sbml2biopax/sbml2biopaxAnnotationParameter/annotationSpecies"/>

  </body>
 </html>
</xsl:template>

 <xsl:template match="sbml2biopax/sbml2biopaxAnnotationParameter/publicationModel">
  <table border="1">
   <xsl:for-each select="publication">
    <tr>
     <td>
      Publication 
     </td>
     <td>
      <xsl:value-of select='.'/>
     </td>
    </tr>
   </xsl:for-each>
   <xsl:for-each select="constraint">
    <tr>
     <td>
      Constraint
     </td>
     <td>
      <xsl:value-of select='.'/>
     </td>
    </tr>
   </xsl:for-each>
  </table>
 </xsl:template>

 <xsl:template match="sbml2biopax/sbml2biopaxAnnotationParameter/speciesAnnotationMap">
  <table border="1">
   <xsl:for-each select="species">
    <tr>
     <td>
      Species 
     </td>
     <td>
      <xsl:value-of select='.'/>
     </td>
    </tr>
   </xsl:for-each>
   <xsl:for-each select="constraint">
    <tr>
     <td>
      Constraint
     </td>
     <td>
      <xsl:value-of select='.'/>
     </td>
    </tr>
   </xsl:for-each>
  </table>
 </xsl:template>

 <xsl:template match="sbml2biopax/sbml2biopaxAnnotationParameter/taxonomyMap">
  <table border="1">
   <xsl:for-each select="taxonomy">
    <tr>
     <td>
      Taxonomy 
     </td>
     <td>
      <xsl:value-of select='.'/>
     </td>
    </tr>
   </xsl:for-each>
   <xsl:for-each select="constraint">
    <tr>
     <td>
      Constraint
     </td>
     <td>
      <xsl:value-of select='.'/>
     </td>
    </tr>
   </xsl:for-each>
  </table>
 </xsl:template>

 <xsl:template match="sbml2biopax/sbml2biopaxAnnotationParameter/annotationSpecies">
  <table border="1">
   <tr>
    <td>
     BioPAX class
    </td>
    <td>
     Database URI(s)
    </td>
   </tr>
   <xsl:for-each select="physicalEntity">
    <tr>
     <td>
      <xsl:value-of select='@classBioPAX'/>
     </td>
     <xsl:for-each select="species">
      <td>
       <xsl:value-of select='.'/>
      </td>
     </xsl:for-each>
     <xsl:for-each select="constraint">
      <td>
       <font color="red">
        <xsl:value-of select='.'/>
       </font>
      </td>
     </xsl:for-each>
    </tr>
   </xsl:for-each>
  </table>
 </xsl:template>
</xsl:stylesheet>
