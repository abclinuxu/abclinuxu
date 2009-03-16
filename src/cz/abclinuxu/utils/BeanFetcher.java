package cz.abclinuxu.utils;

import org.dom4j.Element;
import org.dom4j.Node;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.view.Author;

/**
 * Transforms persistence layer object into JavaBeans used in application
 * 
 * @author kapy
 * 
 */
public class BeanFetcher {

	/**
	 * Determines how data are fetched into bean
	 * 
	 * @author kapy
	 * 
	 */
	public static enum FetchType {
		/**
		 * All data are fetched, including XML parsing
		 */
		EAGER,
		/**
		 * Only data directly accessible from columns of SQL table are fetched,
		 * XML parsing is ommited
		 */
		OMIT_XML
	}

	/**
	public static class ParamMapper {
		private List<ParamMapperElement> elements;
	
		public ParamMapper(String...parts) {
			if(parts.length%3!=0) 
				throw new IllegalArgumentException("Param mapper is not following required syntax, wrong number of arguments!");
			
			elements = new ArrayList<ParamMapperElement>();
			ParamMapperElement pme = null;
			for(int i=0; i< parts.length; i++) {
				switch(i%3) {
				case 0:
					pme = new ParamMapperElement();
					pme.paramName = parts[i];
					break;
				case 1:
					pme.xpath = parts[i];
					break;
				case 2:
					pme.errorMessage = parts[i];
					elements.add(pme);
					break;
				}
			}			
		} 
	}
	
	
	private static class ParamMapperElement {
		String paramName;
		String xpath;
		String errorMessage;
	}
	*/
	
	// Disable instantiation
	private BeanFetcher() {
	}

	/**
	 * Creates author JavaBean object from data fetched out of persistence layer
	 * 
	 * @param item
	 *            Record from persistence layer
	 * @param ft
	 * @return Author object created from provided data
	 */
	public static Author fetchAuthorFromItem(Item item, FetchType ft) {

		if (item == null)
			return null;

		Author author = new Author();

		author.setId(item.getId());
		author.setUid(item.getNumeric1());

		boolean active = (item.getNumeric2() != null && item.getNumeric2()
				.equals(1)) ? true : false;
		author.setActive(active);
		author.setName(item.getString1());
		author.setSurname(item.getString2());

		switch (ft) {
		case OMIT_XML:
			return author;
		case EAGER:
			Element r = item.getData().getRootElement();
			author.setAbout(safeRetrieveElementText(r, "/data/about"));
			author.setAddress(safeRetrieveElementText(r, "/data/address"));
			author.setAccountNumber(safeRetrieveElementText(r,
					"/data/accountNumber"));
			author.setBirthNumber(safeRetrieveElementText(r,
					"/data/birthNumber"));
			author.setEmail(safeRetrieveElementText(r, "/data/email"));
			author.setLogin(safeRetrieveElementText(r, "/data/login"));
			author.setNickname(safeRetrieveElementText(r, "/data/nickname"));
			author.setPhone(safeRetrieveElementText(r, "/data/phone"));
			author.setPhotoUrl(safeRetrieveElementText(r, "/data/photourl"));
			return author;
		default:
			throw new UnsupportedOperationException(
					"Unable to fetch data with FetchType " + ft.toString());
		}
	}
	
	private static String safeRetrieveElementText(Element root, String xpath) {

		Node node = root.selectSingleNode(xpath);
		if (node != null)
			return node.getText();

		return null;

	}

}
