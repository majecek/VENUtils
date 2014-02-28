package com.chowaniok

import spock.lang.Specification
import spock.lang.Unroll


/**
 * User: Marek Chowaniok
 * Date: 28/02/14
 */
class ParsingSavedChartsXMLTest extends Specification {

    def parser = new ParsingSavedChartsXML()

    def setup() {
    }

    def cleanup() {
    }


    @Unroll
    def 'testing parsing inner XML'() {
        when:
        parser.parseInnerXML(xml , "parent")

        then:
        parser.numOfIssues == numberOfIssues

        where:
        xml | numberOfIssues
        "<FilterStack1 pricemart='Pricemart' name='1'> <node /></FilterStack1>" | 0
        "<FilterStack1 pricemart='Pricemart' name='1'> <node ></FilterStack1>" | 1
        "<FilterStack1 pricemart='Pricemart' name='1'> <node ><node></node></FilterStack1>" | 1
    }
}
