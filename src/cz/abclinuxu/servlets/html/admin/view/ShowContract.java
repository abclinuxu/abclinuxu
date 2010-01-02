package cz.abclinuxu.servlets.html.admin.view;

import java.util.List;
import java.util.Map;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.EditionRole;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.ContractTemplate;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.data.view.SignedContract;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.admin.edit.EditContract;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PageNavigation;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.comparator.SignedContractComparator;
import cz.abclinuxu.utils.BeanFetcher.FetchType;
import cz.abclinuxu.utils.freemarker.Tools;
import org.apache.log4j.Logger;

public class ShowContract implements AbcAction {
	private static final Logger log = Logger.getLogger(ShowContract.class);

	public static final String VAR_CONTRACTS = "CONTRACTS";
	public static final String VAR_CONTRACT = "CONTRACT";
	public static final String VAR_TEMPLATE = "CONTRACT_TEMPLATE";
	public static final String VAR_TEMPLATES = "TEMPLATES";
	public static final String VAR_AUTHOR = "AUTHOR";
    public static final String VAR_CONTRACT_TEXT = "CONTRACT_TEXT";
    public static final String VAR_CONTRACT_ERROR = "CONTRACT_ERROR";
	public static final String VAR_EDITOR = "EDITOR";
    public static final String VAR_RELATION = "RELATION";

    public static final String PARAM_CONTRACT_ID = "contractId";
	public static final String PARAM_AUTHOR_ID = "authorId";
    public static final String PARAM_RELATION = "rid";

    public static final String ACTION_SHOW = "show";
	public static final String ACTION_SIGNED_CONTRACTS = "contracts";

	@Override
	public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
		Map params = (Map) env.get(Constants.VAR_PARAMS);
		User user = (User) env.get(Constants.VAR_USER);

		if (ServletUtils.handleMaintainance(request, env)) {
			response.sendRedirect(response.encodeRedirectURL("/"));
			return null;
		}

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        EditionRole role = ServletUtils.getEditionRole(user, request);
        if (role == EditionRole.NONE)
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        boolean editor = (role == EditionRole.EDITOR || role == EditionRole.EDITOR_IN_CHIEF);
        if (editor)
            env.put(VAR_EDITOR, Boolean.TRUE);

        PwdNavigator navigator = new PwdNavigator(env, PageNavigation.EDITION_CONTRACTS);

        SignedContract contract = null;
        ContractTemplate template = null;
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation != null) {
            Tools.sync(relation);
            env.put(VAR_RELATION, relation);

            if (relation.getChild() instanceof Item) {
                Item item = (Item) relation.getChild();
                if (item.getType() == Item.CONTRACT_TEMPLATE) {
                    template = BeanFetcher.fetchContractTemplate(relation, BeanFetcher.FetchType.PROCESS_NONATOMIC);
                    env.put(VAR_TEMPLATE, template);
                } else if (item.getType() == Item.SIGNED_CONTRACT) {
                    contract = BeanFetcher.fetchSignedContract(relation, BeanFetcher.FetchType.PROCESS_NONATOMIC);
                    env.put(VAR_CONTRACT, contract);
                }
            }
        }

		Author author = Tools.getAuthor(user.getId());
        env.put(VAR_AUTHOR, author);

		if (editor) {
            // administration actions
            if (ServletUtils.determineAction(params, ACTION_SIGNED_CONTRACTS))
				return actionShowSignedContracts(request, template, env, navigator);
            if (template != null)
                return actionShowTemplate(request, template, env, navigator);
            if (contract != null)
                return actionShowContract(request, contract, env, navigator);

            return actionShowTemplates(request, env, navigator);
        } else {
            // author actions
            if (contract != null) {
                if (! contract.getAuthor().equals(author))
                    return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

                return actionShowAuthorContract(request, contract, env, navigator);
            }

            return actionAuthorContractList(request, env, navigator);
		}
    }

    private String actionShowTemplates(HttpServletRequest request, Map env, PwdNavigator navigator) {
        env.put(Constants.VAR_PARENTS, navigator.navigate());
        SQLTool sqlTool = SQLTool.getInstance();
        Qualifier[] qualifiers = {Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING};
        List<Relation> items = sqlTool.findItemRelationsWithType(Item.CONTRACT_TEMPLATE, qualifiers);
        Tools.syncList(items);
        List<ContractTemplate> templates = BeanFetcher.fetchContractTemplates(items, FetchType.PROCESS_NONATOMIC);
        env.put(VAR_TEMPLATES, templates);

        return FMTemplateSelector.select("ShowContract", "listTemplates", env, request);
    }

    private String actionShowTemplate(HttpServletRequest request, ContractTemplate contractTemplate, Map env, PwdNavigator navigator) {
		Link tail = new Link(contractTemplate.getTitle(), UrlUtils.PREFIX_ADMINISTRATION + "/redakce/smlouvy/" + contractTemplate.getRelationId(), "Zobrazení šablony smlouvy");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		return FMTemplateSelector.select("ShowContract", "showTemplate", env, request);
	}

    private String actionShowSignedContracts(HttpServletRequest request, ContractTemplate contractTemplate, Map env, PwdNavigator navigator) {
        Link tail = new Link(contractTemplate.getTitle(), UrlUtils.PREFIX_ADMINISTRATION + "/redakce/smlouvy/" + contractTemplate.getRelationId(), "Zobrazení šablony smlouvy");
        env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        List<Relation> children = item.getChildren();
        Tools.syncList(children);

        List<SignedContract> contracts = BeanFetcher.fetchSignedContracts(children, FetchType.EAGER);
        Collections.sort(contracts, new SignedContractComparator());
        env.put(VAR_CONTRACTS, contracts);
        return FMTemplateSelector.select("ShowContract", "listContracts", env, request);
    }

    private String actionShowContract(HttpServletRequest request, SignedContract contract, Map env, PwdNavigator navigator) {
        Link tail = new Link(contract.getTitle(), UrlUtils.PREFIX_ADMINISTRATION + "/redakce/smlouvy/" + contract.getRelationId(), "Zobrazení smlouvy");
        env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

        User user = new User(contract.getUid());
        Author author = Tools.getAuthor(user.getId());
        env.put(VAR_AUTHOR, author);
		return FMTemplateSelector.select("ShowContract", "showContract", env, request);
	}

	private String actionShowAuthorContract(HttpServletRequest request, SignedContract contract, Map env, PwdNavigator navigator) {
        Link tail = new Link(contract.getTitle(), "/redakce/smlouvy/" + contract.getRelationId(), "Zobrazení smlouvy");
        env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		return FMTemplateSelector.select("ShowContract", "showAuthorContract", env, request);
	}

	private String actionAuthorContractList(HttpServletRequest request, Map env, PwdNavigator navigator) {
        SQLTool sqlTool = SQLTool.getInstance();
        Author author = (Author) env.get(VAR_AUTHOR);
		env.put(Constants.VAR_PARENTS, navigator.navigate());

        List<SignedContract> contracts = getContracts(author);
        env.put(VAR_CONTRACTS, contracts);

        Relation relation = sqlTool.findUnsignedContractRelation(author.getUid());
        ContractTemplate template = BeanFetcher.fetchContractTemplate(relation, FetchType.PROCESS_NONATOMIC);

        if (template != null) {
            boolean authorVerified = true;
            StringBuilder sb = new StringBuilder("Prosím ").append(" <a href=\"/sprava/redakce/autori/edit/").
                    append(author.getRelationId()).append("?action=edit\">zadejte</a> ");
            if (author.getAddress() == null) {
                sb.append("svou adresu");
                authorVerified = false;
            }
            if (author.getAccountNumber() == null) {
                if (! authorVerified)
                    sb.append(" a ");
                sb.append("číslo účtu");
                authorVerified = false;
            }
            sb.append('.');

            env.put(VAR_TEMPLATE, template);
            if (authorVerified) {
                try {
                    String text = EditContract.fillContractTemplate(template, author);
                    env.put(VAR_CONTRACT_TEXT, text);
                } catch (Exception e) {
                    log.error("Chyba v šabloně smlouvy " + template.getTitle() + " (relace = " + template.getRelationId() + ")", e);
                    env.put(VAR_CONTRACT_ERROR, "Omlouváme se, ale ve smlouvě je chyba, informujte prosím šefredaktora.");
                }
            } else
                env.put(VAR_CONTRACT_ERROR, sb.toString());
        }

        return FMTemplateSelector.select("ShowContract", "listAuthorContracts", env, request);
	}

	private List<SignedContract> getContracts(Author author) {
		SQLTool sqlTool = SQLTool.getInstance();
		Qualifier[] qualifiers = new Qualifier[] {
            new CompareCondition(Field.OWNER, Operation.EQUAL, author.getUid()), Qualifier.SORT_BY_CREATED
        };
        List<Relation> relations = sqlTool.findItemRelationsWithType(Item.SIGNED_CONTRACT, qualifiers);
        Tools.syncList(relations);
        return BeanFetcher.fetchSignedContracts(relations, FetchType.PROCESS_NONATOMIC);
	}
}
