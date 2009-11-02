package cz.abclinuxu.data.view;

import java.util.Comparator;
import java.util.Date;
import java.util.regex.Pattern;

import cz.abclinuxu.data.ImageAssignable;

/**
 * This class represents contract signed between Employer and Employee.
 * Both employer and employee are represented rather by users than
 * authors, to allow assign contract for example for developers.
 * 
 * @author kapy
 * 
 */
public class Contract implements ImageAssignable<Contract.ContractImage> {

	/**
	 * All available images for contract
	 * 
	 * @author kapy
	 * 
	 */
	public enum ContractImage implements ImageAssignable.AssignedImage {
		SIGNATURE_EMPLOYER
	}

	// identification of contract
	private int id;

	// name of employer
	private String employerName;
	// position of employer in company
	private String employerPosition;
	// path to employer's signature
	private String employerSignature;

	// flag that newer contract was signed
	private boolean obsolete;

	// id of employee
	private Author employee;
	// date when contract was proposed
	private Date proposedDate;
	// date when contract was signed
	private Date signedDate;

	// description of contract
	private String description;
	// version identification
	private String version;
	// title of contract
	private String title;
	// whole content
	private String content;

	// template from which this contract was generated
	private Integer templateId;

	public Contract() {
	}

	public Contract(Contract clone) {
		this.content = clone.content;
		this.description = clone.description;
		if (clone.employee != null) {
			this.employee = new Author(clone.employee.getId());
		}
		if (clone.proposedDate != null)
		    this.proposedDate = new Date(clone.proposedDate.getTime());

		if (clone.signedDate != null)
		    this.signedDate = new Date(clone.signedDate.getTime());

		this.obsolete = clone.obsolete;
		this.employerName = clone.employerName;
		this.employerPosition = clone.employerPosition;
		this.employerSignature = clone.employerSignature;
		this.templateId = clone.templateId;
		this.title = clone.title;
		this.version = clone.version;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the employerName
	 */
	public String getEmployerName() {
		return employerName;
	}

	/**
	 * @param employerName the employerName to set
	 */
	public void setEmployerName(String employerName) {
		this.employerName = employerName;
	}

	/**
	 * @return the employerPosition
	 */
	public String getEmployerPosition() {
		return employerPosition;
	}

	/**
	 * @param employerPosition the employerPosition to set
	 */
	public void setEmployerPosition(String employerPosition) {
		this.employerPosition = employerPosition;
	}

	/**
	 * @return the employerSignature
	 */
	public String getEmployerSignature() {
		return employerSignature;
	}

	/**
	 * @param employerSignature the employerSignature to set
	 */
	public void setEmployerSignature(String employerSignature) {
		this.employerSignature = employerSignature;
	}

	/**
	 * @return the employee
	 */
	public Author getEmployee() {
		return employee;
	}

	/**
	 * @param employee the employee to set
	 */
	public void setEmployee(Author employee) {
		this.employee = employee;
	}

	/**
	 * @return the proposedDate
	 */
	public Date getProposedDate() {
		return proposedDate;
	}

	/**
	 * @param proposedDate the proposedDate to set
	 */
	public void setProposedDate(Date proposedDate) {
		this.proposedDate = proposedDate;
	}

	/**
	 * @return the obsolete
	 */
	public boolean isObsolete() {
		return obsolete;
	}

	/**
	 * @param obsolete the obsolete to set
	 */
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	/**
	 * @return the signedDate
	 */
	public Date getSignedDate() {
		return signedDate;
	}

	/**
	 * @param signedDate the signedDate to set
	 */
	public void setSignedDate(Date signedDate) {
		this.signedDate = signedDate;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the templateId
	 */
	public Integer getTemplateId() {
		return templateId;
	}

	/**
	 * @param templateId the templateId to set
	 */
	public void setTemplateId(Integer templateId) {
		this.templateId = templateId;
	}

	public boolean isAccepted() {
		return this.signedDate != null;
	}

	@Override
	public void assignImage(ContractImage imageId, String imageUrl) {
		switch (imageId) {
		case SIGNATURE_EMPLOYER:
			this.employerSignature = imageUrl;
			break;
		}
	}

	@Override
	public String detractImage(ContractImage imageId) {
		String url = null;
		switch (imageId) {
		case SIGNATURE_EMPLOYER:
			url = this.employerSignature;
			this.employerSignature = null;
			break;
		}
		return url;
	}

	@Override
	public String proposeImageUrl(ContractImage imageId, String suffix) {

		// pattern to find blank characters and replace them by '-' character
		final Pattern spacing = Pattern.compile("\\d+");

		StringBuilder sb = new StringBuilder("images/signatures/employer.");
		switch (imageId) {
		case SIGNATURE_EMPLOYER:
			sb.append(spacing.matcher(employerName).replaceAll("-").toLowerCase());
			break;
		}
		sb.append('.').append(suffix);
		return sb.toString();
	}

	/**
	 * Compares contract according to proposed date.
	 * Note: this comparator imposes orderings that are inconsistent with
	 * equals.
	 * 
	 * @author kapy
	 * 
	 */
	public static class ContractComparator implements Comparator<Contract> {

		// direction of comparison
		private boolean ascending;

		public ContractComparator() {
			this(true);
		}

		public ContractComparator(boolean ascending) {
			this.ascending = ascending;
		}

		@Override
		public int compare(Contract o1, Contract o2) {
			if (ascending)
				return compare(o1.getProposedDate(), o2.getProposedDate());
			else
				return compare(o2.getProposedDate(), o1.getProposedDate());
		}

		private int compare(Date d1, Date d2) {
			if (d1 == null && d2 == null) return 0;
			if (d1 == null) return -1;
			if (d2 == null) return 1;
			return d1.compareTo(d2);
		}
	}

}
