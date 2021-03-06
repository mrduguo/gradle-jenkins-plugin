package com.terrafolio.gradle.plugins.jenkins.test.dsl

import com.terrafolio.gradle.plugins.jenkins.dsl.ViewDSLSupport
import com.terrafolio.gradle.plugins.jenkins.jobdsl.MapJobManagement
import com.terrafolio.gradle.plugins.jenkins.test.TempDirSpec
import javaposse.jobdsl.dsl.JobManagement
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

/**
 * Created on 6/27/14.
 */
class ViewDSLSupportTest extends TempDirSpec {
    def ViewDSLSupport support
    def JobManagement mockJobManagement

    def setup() {
        mockJobManagement = Mock(JobManagement)
        support = new ViewDSLSupport(mockJobManagement)
    }

    def "addConfig adds a config to JobManagement" () {
        when:
        support.addConfig("test", "XXX")

        then:
        1 * mockJobManagement.createOrUpdateView("test", "XXX", true)
    }

    def "getConfig returns a config from JobManagement" () {
        when:
        support.getConfig("test")

        then:
        1 * mockJobManagement.getConfig("test")
    }

    def "setParameter sets JobManagement parameter" () {
        setup:
        Map map = new HashMap<String, String>()

        when:
        support.setParameter("test", "value")

        then:
        1 * mockJobManagement.getParameters() >> { map }
        map.test == "value"
    }

    def "evaluateDSL from file creates correct XML" () {
        setup:
        File file = file("test.dsl",
                """
                    view {
                        name "test"
                    }
                """
        )
        support.jobManagement = new MapJobManagement(new HashMap<String, String>())
        XMLUnit.setIgnoreWhitespace(true)

        expect:
        support.evaluateDSL(file) == 'test'
        new Diff(ViewFixtures.LIST_DSL_VIEW_XML, support.getConfig('test')).similar()
    }

    def "evaluateDSL from file creates correct XML (nestedView)" () {
        setup:
        File file = file("test.dsl",
                """
                    view(type: NestedView) {
                        name 'test'
                    }
                """
        )
        support.jobManagement = new MapJobManagement(new HashMap<String, String>())
        XMLUnit.setIgnoreWhitespace(true)

        expect:
        support.evaluateDSL(file) == 'test'
        new Diff(ViewFixtures.NESTED_DSL_VIEW_XML, support.getConfig('test')).similar()
    }

    def "evaluateDSL from file creates correct XML (sectionedView)" () {
        setup:
        File file = file("test.dsl",
                """
                    view(type: SectionedView) {
                        name 'test'
                    }
                """
        )
        support.jobManagement = new MapJobManagement(new HashMap<String, String>())
        XMLUnit.setIgnoreWhitespace(true)

        expect:
        support.evaluateDSL(file) == 'test'
        new Diff(ViewFixtures.SECTIONED_DSL_VIEW_XML, support.getConfig('test')).similar()
    }

    def "evaluateDSL from closure creates correct XML" () {
        setup:
        Closure dsl = {
            name "test"
        }
        support.jobManagement = new MapJobManagement(new HashMap<String, String>())
        XMLUnit.setIgnoreWhitespace(true)

        expect:
        support.evaluateDSL('test', viewType, dsl) == 'test'
        new Diff(xmlToMatch, support.getConfig('test')).similar()

        where:
        viewType            | xmlToMatch
        null                | ViewFixtures.LIST_DSL_VIEW_XML
        'ListView'          | ViewFixtures.LIST_DSL_VIEW_XML
        'BuildPipelineView' | ViewFixtures.BUILD_PIPELINE_VIEW_XML
        'NestedView'        | ViewFixtures.NESTED_DSL_VIEW_XML
        'SectionedView'     | ViewFixtures.SECTIONED_DSL_VIEW_XML
    }
}
