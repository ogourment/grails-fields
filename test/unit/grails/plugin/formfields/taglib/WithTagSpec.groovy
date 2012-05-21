package grails.plugin.formfields.taglib

import grails.plugin.formfields.mock.Person
import spock.lang.Issue
import grails.plugin.formfields.*
import grails.test.mixin.*

@Issue('https://github.com/robfletcher/grails-fields/issues/13')
@TestFor(FormFieldsTagLib)
@Mock(Person)
class WithTagSpec extends AbstractFormFieldsTagLibSpec {

	def mockFormFieldsTemplateService = Mock(FormFieldsTemplateService)

	def setupSpec() {
		configurePropertyAccessorSpringBean()
	}

	def setup() {
		def taglib = applicationContext.getBean(FormFieldsTagLib)

		mockFormFieldsTemplateService.findTemplate(_, 'field') >> [path: '/_fields/default/field']
		taglib.formFieldsTemplateService = mockFormFieldsTemplateService
	}

	void 'bean attribute does not have to be specified if it is in scope from f:with'() {
		given:
		views["/_fields/default/_field.gsp"] = '${property} '

		expect:
		applyTemplate('<f:with bean="personInstance"><f:field property="name"/></f:with>', [personInstance: personInstance]) == 'name '
	}

	void 'scoped bean attribute does not linger around after f:with tag'() {
		expect:
		applyTemplate('<f:with bean="personInstance">${pageScope.getVariable("f:with:bean")}</f:with>${pageScope.getVariable("f:with:bean")}', [personInstance: personInstance]) == 'Bart Simpson'
	}


	@Issue('https://github.com/robfletcher/grails-fields/issues/45')
	void 'with tag doesn\'t render inputs with show'() {
		given:
		def notUsed = '*NOT USED*'
		views["/_fields/default/_field.gsp"] = notUsed
		
		when:
		def output = applyTemplate('''<f:with bean="personInstance" fieldTemplateName="showField" widgetTemplateName="show">
		    <f:field property="name" />
		    <f:field property="dateOfBirth" />
		    <f:field property="address.street" />
		    <f:field property="address.city" />
		    <f:field property="address.country" />
		    <f:field property="gender" />
		    <f:field property="minor" />
		    <f:field property="password" />
		</f:with>''', [personInstance: personInstance])

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