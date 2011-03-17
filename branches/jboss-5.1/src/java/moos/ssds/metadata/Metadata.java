package moos.ssds.metadata;

import java.util.Collection;
import java.util.Iterator;

/**
 * This is largely a convenience class for the XML<->Java translation. Since XML
 * needs a root, we created this root to allow for a variety of XML tags to be
 * the "base" of a valid SSDS Document.
 * 
 * @author kgomes
 * 
 */
public class Metadata {

	private Collection<DataContainer> dataContainers = null;

	private Collection<DataContainerGroup> dataContainerGroups = null;

	private Collection<DataProducer> dataProducers = null;

	private Collection<DataProducerGroup> dataProducerGroups = null;

	private Collection<DataProducer> deployments = null;

	private Collection<Device> devices = null;

	private Collection<Event> events = null;

	private Collection<Keyword> keywords = null;

	private Collection<Person> persons = null;

	private Collection<DataProducer> processRuns = null;

	private Collection<Resource> resources = null;

	private Collection<Software> softwares = null;

	private Collection<StandardDomain> standardDomains = null;

	private Collection<StandardKeyword> standardKeywords = null;

	private Collection<StandardReferenceScale> standardReferenceScales = null;

	private Collection<StandardUnit> standardUnits = null;

	private Collection<StandardVariable> standardVariables = null;

	public Collection<DataContainer> getDataContainers() {
		return dataContainers;
	}

	public void setDataContainers(Collection<DataContainer> dataContainers) {
		this.dataContainers = dataContainers;
	}

	public Collection<DataContainerGroup> getDataContainerGroups() {
		return dataContainerGroups;
	}

	public void setDataContainerGroups(
			Collection<DataContainerGroup> dataContainerGroups) {
		this.dataContainerGroups = dataContainerGroups;
	}

	public Collection<DataProducer> getDataProducers() {
		return dataProducers;
	}

	public void setDataProducers(Collection<DataProducer> dataProducers) {
		this.dataProducers = dataProducers;
	}

	public Collection<DataProducerGroup> getDataProducerGroups() {
		return dataProducerGroups;
	}

	public void setDataProducerGroups(
			Collection<DataProducerGroup> dataProducerGroups) {
		this.dataProducerGroups = dataProducerGroups;
	}

	public Collection<DataProducer> getDeployments() {
		return deployments;
	}

	public void setDeployments(Collection<DataProducer> deployments) {
		this.deployments = deployments;
	}

	public Collection<Device> getDevices() {
		return devices;
	}

	public void setDevices(Collection<Device> devices) {
		this.devices = devices;
	}

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

	public Collection<DataProducer> getProcessRuns() {
		return processRuns;
	}

	public void setProcessRuns(Collection<DataProducer> processRuns) {
		this.processRuns = processRuns;
	}

	public Collection<Resource> getResources() {
		return resources;
	}

	public void setResources(Collection<Resource> resources) {
		this.resources = resources;
	}

	public Collection<Software> getSoftwares() {
		return softwares;
	}

	public void setSoftwares(Collection<Software> softwares) {
		this.softwares = softwares;
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

	public String toStringRepresentation(String delimeter) {
		StringBuilder messageBuilder = new StringBuilder();
		if (dataContainers != null && dataContainers.size() > 0) {
			for (Iterator<DataContainer> iterator = dataContainers.iterator(); iterator
					.hasNext();) {
				messageBuilder.append(iterator.next().toStringRepresentation(
						delimeter)
						+ "\n");
			}
		}
		if (dataContainerGroups != null && dataContainerGroups.size() > 0) {
			for (Iterator<DataContainerGroup> iterator = dataContainerGroups
					.iterator(); iterator.hasNext();) {
				messageBuilder.append(iterator.next().toStringRepresentation(
						delimeter)
						+ "\n");

			}
		}
		if (dataProducers != null && dataProducers.size() > 0) {
			for (Iterator<DataProducer> iterator = dataProducers.iterator(); iterator
					.hasNext();) {
				messageBuilder.append(iterator.next().toStringRepresentation(
						delimeter)
						+ "\n");

			}
		}
		if (dataProducerGroups != null && dataProducerGroups.size() > 0) {
			for (Iterator<DataProducerGroup> iterator = dataProducerGroups
					.iterator(); iterator.hasNext();) {
				messageBuilder.append(iterator.next().toStringRepresentation(
						delimeter)
						+ "\n");

			}
		}
		return messageBuilder.toString();
	}
}
