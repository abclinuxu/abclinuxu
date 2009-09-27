package cz.abclinuxu.data.view;

import java.util.Date;

import cz.abclinuxu.data.ImageAssignable;

/**
 * This class represents contract signed between Employer and Employee
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
	private Integer employer;
	// path to employer's signature
	private String employerSignature;
	// id of employee
	private Integer employee;
	// path to employee's signature
	private String employeeSignature;
	// date when contract was signed
	private Date effectiveDate;

	// version identificator
	private String version;
	// title of contract
	private String title;
	// whole content
	private String content;



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
	public Integer getEmployer() {
		return employer;
	}

	/**
	 * @param employer the employer to set
	 */
	public void setEmployer(Integer employer) {
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
	public Integer getEmployee() {
		return employee;
	}

	/**
	 * @param employee the employee to set
	 */
	public void setEmployee(Integer employee) {
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
		StringBuilder sb = new StringBuilder("images/signatures/");
		switch (imageId) {
		case SIGNATURE_EMPLOYEE:
			sb.append(employee);
			break;
		case SIGNATURE_EMPLOYER:
			sb.append(employer);
			break;
		}
		sb.append('.').append(suffix);
		return sb.toString();
	}
	
	public boolean isSigned() {
		return effectiveDate != null;
	}
}
