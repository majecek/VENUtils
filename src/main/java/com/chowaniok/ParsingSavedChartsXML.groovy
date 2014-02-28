package com.chowaniok

import org.xml.sax.SAXParseException

/**
 * User: Marek Chowaniok
 * Date: 28/02/14
 */
class ParsingSavedChartsXML {

    def numOfIssues = 0

    def returnSlurper(String path) {
        def file = new File(path)
        def slurper = new XmlSlurper()
        def sch = slurper.parse(file)
    }

    def parseInnerXML(text, parentUniqueName){
        def slurper = new XmlSlurper()
        def xml
        try{
            xml = slurper.parseText(text)
        }catch(SAXParseException e){
            println "Parsing Error: "+e + " in: " + parentUniqueName
            numOfIssues += 1
        }

    }

    def parseXML(String path){
        def entity_data = returnSlurper(path)
        entity_data.VReportParameters.each{
            parseInnerXML(it.VXMLQuery.toString(), it.VUniqueName)
        }
    }

    public static void main(String[] args) {
        def sch = new ParsingSavedChartsXML()
        sch.parseXML('/Users/marek/Desktop/savedcharts_fixed.xml')
        println sch.numOfIssues
    }
}
