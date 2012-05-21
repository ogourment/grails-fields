package grails.plugin.formfields.taglib

import grails.plugin.formfields.mock.Person
import grails.plugin.formfields.*
import grails.test.mixin.*
import spock.lang.*

@TestFor(FormFieldsTagLib)
@Mock(Person)
@Unroll
class AllTagSpec extends AbstractFormFieldsTagLibSpec {

	def mockFormFieldsTemplateService = Mock(FormFieldsTemplateService)

	def setupSpec() {
		configurePropertyAccessorSpringBean()
	}

	def setup() {
		def taglib = applicationContext.getBean(FormFieldsTagLib)

		mockFormFieldsTemplateService.findTemplate(_, 'field') >> [path: '/_fields/default/field']
		taglib.formFieldsTemplateService = mockFormFieldsTemplateService

		mockEmbeddedSitemeshLayout(taglib)
	}

	void "all tag renders fields for all properties"() {
		given:
		views["/_fields/default/_field.gsp"] = '${property} '

		when:
		def output = applyTemplate('<f:all bean="personInstance"/>', [personInstance: personInstance])

		then:
		output =~ /\bname\b/ // \b = word boundary
		output =~ /\bpassword\b/
		output =~ /\bgender\b/
		output =~ /\bdateOfBirth\b/
		output =~ /\bminor\b/
	}

	@Issue('https://github.com/robfletcher/grails-fields/issues/21')
	void 'all tag skips #property property'() {
		given:
		views["/_fields/default/_field.gsp"] = '${property} '

		when:
		def output = applyTemplate('<f:all bean="personInstance"/>', [personInstance: personInstance])

		then:
		!output.contains(property)

		where:
		property << ['id', 'version', 'onLoad', 'lastUpdated', 'excludedProperty', 'displayFalseProperty']
	}

	@Issue('https://github.com/robfletcher/grails-fields/issues/12')
	void 'all tag skips properties listed with the except attribute'() {
		given:
		views["/_fields/default/_field.gsp"] = '${property} '

		when:
		def output = applyTemplate('<f:all bean="personInstance" except="password, minor"/>', [personInstance: personInstance])

		then:
		!output.contains('password')
		!output.contains('minor')
	}

	@Issue('https://github.com/robfletcher/grails-fields/issues/45')
	void 'all tag doesn\'t render inputs with show'() {
		given:
		def notUsed = '*NOT USED*'
		views["/_fields/default/_field.gsp"] = notUsed 
		
		when:
		def output = applyTemplate('<f:all bean="personInstance" fieldTemplateName="showField" widgetTemplateName="show"/>', [personInstance: personInstance])

		then:
		!output.contains(notUsed)
		output =~ /aria-labelledby='name-label'>Bart Simpson</
		output =~ /aria-labelledby='dateOfBirth-label'>.*\b1987</
		output =~ /aria-labelledby='address.street-label'>94 Evergreen Terrace</
		output =~ /aria-labelledby='address.city-label'>Springfield</
		output =~ /aria-labelledby='address.country-label'>USA</
		output =~ /aria-labelledby='gender-label'>Male</
		output =~ /aria-labelledby='minor-label'>true</
		output =~ /aria-labelledby='password-label'>bartman</
	}
}