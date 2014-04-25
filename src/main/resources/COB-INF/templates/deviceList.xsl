<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
    <html>
      <head>
        <title>Welcome to EMS</title>
      </head>
      <body>
        <h2>Below are your active devices.</h2>
        <ul>
        	<xsl:if test="not(Registry/Device)">
        		<h3>No active devices now...</h3>
        	</xsl:if>
        	<xsl:apply-templates/>
        </ul>
      </body>
    </html>
  </xsl:template>
	
  
  <xsl:template match="Device">
  	<li><a href="dev/{@id}"><xsl:value-of select="Name"/></a></li>
  	
  </xsl:template>

</xsl:stylesheet>