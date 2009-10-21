package cz.abclinuxu.data.view;

import java.util.Date;

import cz.abclinuxu.data.ImageAssignable;
import cz.abclinuxu.data.User;

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
		SIGNATURE_EMPLOYER,
		SIGNATURE_EMPLOYEE
	}

	// identification of contract
	private int id;

	// id of employer
	private User employer;
	// path to employer's signature
	private String employerSignature;
	// id of employee
	private User employee;
	// path to employee's signature
	private String employeeSignature;
	// date when contract was signed
	private Date effectiveDate;
	// date when contract was proposed
	private Date proposedDate;

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
		if (clone.effectiveDate != null)
		    this.effectiveDate = new Date(clone.getEffectiveDate().getTime());
		if (clone.employee != null)
		    this.employee = new User(clone.getEmployee().getId());
		this.employeeSignature = clone.employeeSignature;
		if (clone.employer != null)
		    this.employer = new User(clone.getEmployer().getId());
		this.employerSignature = clone.employerSignature;
		if (clone.proposedDate != null)
		    this.proposedDate = new Date(clone.getProposedDate().getTime());
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
	 * @return the employer
	 */
	public User getEmployer() {
		return employer;
	}

	/**
	 * @param employer the employer to set
	 */
	public void setEmployer(User employer) {
		this.employer = employer;
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
	public User getEmployee() {
		return employee;
	}

	/**
	 * @param employee the employee to set
	 */
	public void setEmployee(User employee) {
		this.employee = employee;
	}

	/**
	 * @return the employeeSignature
	 */
	public String getEmployeeSignature() {
		return employeeSignature;
	}

	/**
	 * @param employeeSignature the employeeSignature to set
	 */
	public void setEmployeeSignature(String employeeSignature) {
		this.employeeSignature = employeeSignature;
	}

	/**
	 * @return the effectiveDate
	 */
	public Date getEffectiveDate() {
		return effectiveDate;
	}

	/**
	 * @param effectiveDate the effectiveDate to set
	 */
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
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

	@Override
	public void assignImage(ContractImage imageId, String imageUrl) {
		switch (imageId) {
		case SIGNATURE_EMPLOYEE:
			this.employeeSignature = imageUrl;
			break;
		case SIGNATURE_EMPLOYER:
			this.employerSignature = imageUrl;
			break;
		}
	}

	@Override
	public String detractImage(ContractImage imageId) {
		String url = null;
		switch (imageId) {
		case SIGNATURE_EMPLOYEE:
			url = this.employeeSignature;
			this.employeeSignature = null;
			break;
		case SIGNATURE_EMPLOYER:
			url = this.employerSignature;
			this.employerSignature = null;
		}
		return url;
	}

	@Override
	public String proposeImageUrl(ContractImage imageId, String suffix) {
		StringBuilder sb = new StringBuilder("images/signatures/user.");
		switch (imageId) {
		case SIGNATURE_EMPLOYEE:
			sb.append(employee.getId());
			break;
		case SIGNATURE_EMPLOYER:
			sb.append(employer.getId());
			break;
		}
		sb.append('.').append(suffix);
		return sb.toString();
	}

	public boolean isSigned() {
		return effectiveDate != null;
	}
}
