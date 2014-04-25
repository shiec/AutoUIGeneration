<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="spec">
<html>
	<head>
		<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js">o</script>
		<script>
			<xsl:text disable-output-escaping="yes">
			function save(current){
				var select_value = $('#'+current).val();
				var select_name = current;
				var intRegex = /^\d+$/;
				$.post($('#url').text()+'save',{name:select_name, value:select_value},function(data){
					if(data){
						alert($('#'+data).text());
					}else{
						$('#state_'+select_name).html(select_value);
					}
				});
			}
			
			function send(name){
				$.post($('#url').text()+'runtime',{name:name}, function(data){
					if(data.indexOf(':')!= -1){
						alert($('#Incomplete').text() + '(' + data + ')');
					}else{
						alert($('#'+data).text());
					}
				});
			}
			
			function getParam(param_value){
				$.post($('#url').text()+'getParam',{param:param_value}, function(data){
					if(!data||data=="null"){
						alert($('#Unavailable').text());
						$('#state_'+param_value).html('N/A');
					}else{
						$('#state_'+param_value).html(data);
					}
				});
				
			}
			</xsl:text>
		</script>
	</head>
	<body>
		<ul style="visibility: hidden">
			<li id="url"><xsl:value-of select="@url"/></li>
		</ul>
		<xsl:apply-templates select="manufact"/>
		<xsl:apply-templates select="operations"/>
		<xsl:apply-templates select="notifications"/>
	</body>
</html>
</xsl:template>

<xsl:template match="manufact">
	<h3>Manufacture Information</h3>
		<table border="1">
		<tr bgcolor="#9acd32">
			<xsl:for-each select="label">
                <th><xsl:value-of select="./@name"/></th>
			</xsl:for-each>
		</tr>
		<tr>
			<xsl:for-each select="label">
				<td><xsl:value-of select="value"/></td>
			</xsl:for-each>
		</tr>
		</table>
<br/>
	<xsl:call-template name="statedata"/>
</xsl:template>

<xsl:template name="statedata">
	<h3>Current settings</h3>
	<ul>
	  <xsl:for-each select="../statedata/variable">
	  	<li><xsl:value-of select="./@name"/>: <span id="state_{./@name}"><xsl:value-of select="."/></span></li>
	  </xsl:for-each>
	</ul>
</xsl:template>

<xsl:template match="operations">
	<xsl:apply-templates select="operation"/>
</xsl:template>
	
<xsl:template match="notifications">
	<xsl:apply-templates select="notification"/>
</xsl:template>

<xsl:template match="notification">
	<span id="{@name}" style="visibility: hidden"><xsl:value-of select="name"/></span>
</xsl:template>

<xsl:template match="operation">
	<xsl:choose>
		<xsl:when test="returns">
				<input type="button" onClick="getParam('{returns/return/@name}')" value="{name}"/>
		</xsl:when>
		<xsl:otherwise>
			<h4><xsl:value-of select="name"/>
			<xsl:if test="active-if">
				<i>(Active if <xsl:value-of select="active-if/@select"/> is set to <xsl:value-of select="active-if/include/@select"/>.)</i>
			</xsl:if>
			</h4>
			<xsl:choose>
				<xsl:when test="parameters">
					<xsl:apply-templates select="parameters"/>
					<input type="button" onClick="save('{parameters/parameter/@name}')" value="Save"/>
				</xsl:when>
				<xsl:otherwise>
					<input type="button" onClick="send('{name}')" value="{name}"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>
	
<xsl:template match="parameters">
		<xsl:apply-templates select="parameter"/>
</xsl:template>

<xsl:template match="parameter">
	<xsl:value-of select="@name"/><xsl:if test="@unit">(<xsl:value-of select="@unit"/>)</xsl:if>:
	<xsl:choose>
		<xsl:when test="selection/@num &gt; 0">
			<xsl:choose>
				<xsl:when test="selection/@multiple">
					<select multiple="true" id="{@name}">
						<xsl:for-each select="selection/slct">
							<option value="{value}"><xsl:value-of select="value"/></option>
						</xsl:for-each>
					</select>
				</xsl:when>
				<xsl:otherwise>
					<select id="{@name}">
						<xsl:for-each select="selection/slct">
							<option value="{value}"><xsl:value-of select="value"/></option>
						</xsl:for-each>
					</select>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:when>
				
		<xsl:when test="selection/@num = 0">
			<input type="text" name="{@name}" id="{@name}"/>
			<xsl:if test="selection/min and selection/max">
				<i>(Choose from <xsl:value-of select="selection/min"/>--<xsl:value-of select="selection/max"/>)<span id="{@name}Type" style="visibility: hidden"><xsl:value-of select="type"/></span></i>
			</xsl:if>
		</xsl:when>
	</xsl:choose>	
</xsl:template>

</xsl:stylesheet>