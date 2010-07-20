package moos.ssds.metadata;

import java.util.Collection;

/**
 * This is largely a convenience class for the XML<->Java translation. Since XML
 * needs a root, we created this root to allow for a variety of XML tags to be
 * the "base" of a valid SSDS Document.
 * 
 * @author kgomes
 * 
 */
public class Metadata {

	private Collection<Event> events = null;

	private Collection<Keyword> keywords = null;

	private Collection<Person> persons = null;

	private Collection<Resource> resources = null;

	private Collection<StandardDomain> standardDomains = null;

	private Collection<StandardKeyword> standardKeywords = null;

	private Collection<StandardReferenceScale> standardReferenceScales = null;

	private Collection<StandardUnit> standardUnits = null;

	private Collection<StandardVariable> standardVariables = null;

	public Collection<Event> getEvents() {
		return events;
	}

	public void setEvents(Collection<Event> events) {
		this.events = events;
	}

	public Collection<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(Collection<Keyword> keywords) {
		this.keywords = keywords;
	}

	public Collection<Person> getPersons() {
		return persons;
	}

	public void setPersons(Collection<Person> persons) {
		this.persons = persons;
	}

	public Collection<Resource> getResources() {
		return resources;
	}

	public void setResources(Collection<Resource> resources) {
		this.resources = resources;
	}

	public Collection<StandardDomain> getStandardDomains() {
		return standardDomains;
	}

	public void setStandardDomains(Collection<StandardDomain> standardDomains) {
		this.standardDomains = standardDomains;
	}

	public Collection<StandardKeyword> getStandardKeywords() {
		return standardKeywords;
	}

	public void setStandardKeywords(Collection<StandardKeyword> standardKeywords) {
		this.standardKeywords = standardKeywords;
	}

	public Collection<StandardReferenceScale> getStandardReferenceScales() {
		return standardReferenceScales;
	}

	public void setStandardReferenceScales(
			Collection<StandardReferenceScale> standardReferenceScales) {
		this.standardReferenceScales = standardReferenceScales;
	}

	public Collection<StandardUnit> getStandardUnits() {
		return standardUnits;
	}

	public void setStandardUnits(Collection<StandardUnit> standardUnits) {
		this.standardUnits = standardUnits;
	}

	public Collection<StandardVariable> getStandardVariables() {
		return standardVariables;
	}

	public void setStandardVariables(
			Collection<StandardVariable> standardVariables) {
		this.standardVariables = standardVariables;
	}

}
