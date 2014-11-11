package de.hpi.semrecsys.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.gagawa.java.Node;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.B;
import com.hp.gagawa.java.elements.Body;
import com.hp.gagawa.java.elements.Br;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.H1;
import com.hp.gagawa.java.elements.H3;
import com.hp.gagawa.java.elements.Head;
import com.hp.gagawa.java.elements.Hr;
import com.hp.gagawa.java.elements.Html;
import com.hp.gagawa.java.elements.Img;
import com.hp.gagawa.java.elements.Meta;
import com.hp.gagawa.java.elements.P;
import com.hp.gagawa.java.elements.Span;
import com.hp.gagawa.java.elements.Table;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Th;
import com.hp.gagawa.java.elements.Thead;
import com.hp.gagawa.java.elements.Tr;

import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.model.Attribute;
import de.hpi.semrecsys.model.AttributeEntity;
import de.hpi.semrecsys.model.Entity;
import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.persistence.EntityDAO;
import de.hpi.semrecsys.similarity.AttributeEntityMapping;
import de.hpi.semrecsys.similarity.EntitySimilarityMatrix.EntityTuple;

/**
 * Creates output in HTML form for recommendations
 * @author Michael Wolowyk
 *
 */
public class HTMLOutputCreator {
	String productLinkPath = "catalog/product/view/id/";
	protected Body body = new Body();
	Html html = new Html();
	private String customerLogo;
	private static SemRecSysConfigurator configurator;

	public HTMLOutputCreator(SemRecSysConfigurator configurator) {
		HTMLOutputCreator.configurator = configurator;
		String customerWebpage = configurator.getJsonProperties().getCustomerWebsite();
		customerLogo = configurator.getJsonProperties().getCustomerLogo();
		productLinkPath = customerWebpage + productLinkPath;
	}

	public HTMLOutputCreator(SemRecSysConfigurator configurator, Product product) {
		this(configurator);
		initHtml(product);
	}

	public Html getHtml() {
		return html;
	}

	public String getHtmlString() {
		return html.write();
	}

	protected void initHtml(Product product) {
		String title = "Semantic Recommender System";
		Img customerLogoImg = new Img("customerLogo", customerLogo);
		customerLogoImg.setWidth("150px");
		Head head = createHead();
		body = initBody(title, customerLogoImg, product);
		html.appendChild(head);
		html.appendChild(body);
	}

	protected Body initBody(String title, Img customerLogoImg, Product product) {
		H1 header = new H1();
		header.appendChild(customerLogoImg);
		header.appendText(title);
		body.appendChild(header);

		H3 baseProductHeader = new H3();
		baseProductHeader.appendText("For product " + product + "</br>");
		body.appendChild(baseProductHeader);

		Node productToHTML = productToHTMLTable(product, null);
		body.appendChild(productToHTML);

		Hr hrline = new Hr();
		body.appendChild(hrline);

		H3 recommendationHeader = new H3();
		recommendationHeader.appendText(" following recommendations were found: </br>");
		body.appendChild(recommendationHeader);
		return body;
	}

	/**
	 * Add recommendation entry to the output HTML
     * @param recommendationResult
     */
	public void addRecommendationEntry(RecommendationResult recommendationResult) {
		Map<String, Double> attributesByType = configurator.getJsonProperties().getAttributesByType();
		Map<String, List<EntityTuple>> commonEntitiesMap = recommendationResult.getCommonEntitiesMap();
		Table recommendation = productToHTMLTable(recommendationResult);
		if (commonEntitiesMap != null) {
			String commonEntitiesToString = "<h4>Related entities: </h4>"
					+ recommendationResult.commonEntitiesToHTML(attributesByType);
			Tr commonEntitiesFooter = createFooter(commonEntitiesToString);
			recommendation.appendChild(commonEntitiesFooter);

			// String calculationFooterString = "<b>Calculation: </b></br>"
			// + recommendationResult.calculationToString(attributesByType);
			// Tr calculationFooter = createFooter(calculationFooterString);
			// recommendation.appendChild(calculationFooter);
		}
		body.appendChild(recommendation);
	}

	private Tr createFooter(String footerString) {
		Tr tr = new Tr();
		Td td = new Td();
		td.setAttribute("colspan", "2");
		td.appendText(footerString);
		tr.appendChild(td);
		return tr;
	}

	private Head createHead() {
		Head head = new Head();
		Meta contextTypeMeta = getMetaUtf8();
		head.appendChild(contextTypeMeta);
		return head;
	}

	private Meta getMetaUtf8() {
		Meta contextTypeMeta = new Meta("text/html;charset=utf-8");
		contextTypeMeta.setHttpEquiv("Content-Type");
		return contextTypeMeta;
	}

	protected Table productToHTMLTable(RecommendationResult recommendationResult) {
		Product product = recommendationResult.recommendedProduct();
        String type = recommendationResult.getType() + " " + recommendationResult.getId().getPosition();
		String recommendationScoreString = type + " " + recommendationResult.recommendationScoreToString();
		return productToHTMLTable(product, recommendationScoreString);
	}

	protected Table productToHTMLTable(Product product, String recommendationScoreString) {
		Table productTable = new Table();
		Tr productTr = new Tr();

		Td imgTd = createImgTdNode(product.getImgPathes(), 165);

		List<Node> tdNodes = new ArrayList<Node>();
		Thead tableHeader = createTableHeader(product, recommendationScoreString, "2");
		productTable.appendChild(tableHeader);
		productTable.setBorder("solid 1px");
		Td tdAttributes = createAttributesTdNode(product);
		tdNodes.add(imgTd);
		tdNodes.add(tdAttributes);

		productTr.appendChild(tdNodes);
		productTable.appendChild(productTr);

		AttributeEntityMapping holder = product.getAttributeEntityMapping();
		if (holder != null) {
			String footerString = foundEntitiesString(holder);
			Tr footer = createFooter(footerString);
			productTable.appendChild(footer);
		}
		return productTable;
	}

	private String foundEntitiesString(AttributeEntityMapping holder) {
		StringBuilder builder = new StringBuilder();
		builder.append("<b>Found entities: </b></br>");
		Map<String, Map<Entity, AttributeEntity>> attributeEntitys = holder.getGroupedAttributeEntities();
		for (String attribute : attributeEntitys.keySet()) {
			Map<Entity, AttributeEntity> attributeEntities = attributeEntitys.get(attribute);

			builder.append("<b>" + attribute + ": </b>");
			int count = 0;
			for (AttributeEntity aew : attributeEntities.values()) {

				if (count > 0) {
					builder.append(", ");
				}
				builder.append(entityToLink(aew.getEntity()) + "(" + aew.getCount() + ")");
				count++;
			}
			builder.append("</br>");
		}

		return builder.toString();
	}

	protected Td createAttributesTdNode(Product product) {
		P pAttributes = new P();
		for (String attributeCode : product.getAttributes().keySet()) {
			B b = new B();
			b.appendText(attributeCode + ": ");
			pAttributes.appendChild(b);
			int idx = 0;
			for (Attribute attribute : product.getAttributes().get(attributeCode)) {
				if (idx > 0) {
					pAttributes.appendText(", ");
				}
				String attributeValue = attribute.getValue();
				if (attributeCode.equalsIgnoreCase("category")) {
					attributeValue = attributeValue.split("_")[1];
				}
				pAttributes.appendText(attributeValue);
				idx++;
			}
			Br br = new Br();

			pAttributes.appendChild(br);
		}

		Td tdAttributes = new Td();
		tdAttributes.appendChild(pAttributes);
		return tdAttributes;
	}

	/**
	 * * <thead>
	 * <tr>
	 * <th colspan="2"><a href="www.naturideen.de" ><b>Armreif aus Rochenleder
	 * mit Silberenden 10mm - Schwarz</a></th>
	 * </tr>
	 * </thead>
	 * 
	 * @param product
	 * @param recommendationScoreString
	 * @return
	 */
	protected Thead createTableHeader(Product product, String recommendationScoreString, String colspan) {
		Thead thead = new Thead();
		Tr tr = new Tr();
		Th th = new Th();
		th.setAttribute("colspan", colspan);
		A link = getProductLink(product);
		th.appendChild(link);
		Div productIdDiv = new Div();
		productIdDiv.appendText(String.valueOf(product.getProductId()));
		productIdDiv.setCSSClass("hide");
		th.appendChild(productIdDiv);
		appendString(recommendationScoreString, th);
		tr.appendChild(th);
		thead.appendChild(tr);

		return thead;
	}

	protected Thead createTableHeader(Product product) {
		Thead thead = new Thead();
		Tr tr = new Tr();
		Th th = new Th();
		th.setAttribute("colspan", "2");
		A link = getProductLink(product);
		th.appendChild(link);
		tr.appendChild(th);
		thead.appendChild(tr);

		return thead;
	}

	protected A getProductLink(Product product) {
		A link = new A(productLinkPath + product.getProductId());

		link.appendText(product.getTitle());
		return link;
	}

	private void appendString(String recommendationScoreString, Th th) {
		if (recommendationScoreString != null) {
			String similarityString = recommendationScoreString;
			H3 h3 = new H3();
			h3.appendText(similarityString);
			th.appendChild(h3);
		}
	}

	protected Td createImgTdNode(List<String> imgPathes, int width) {
		Td imgTd = new Td();
		int idx = 1;
		List<Img> imgs = getImgs(imgPathes, width);
		for (Img productImg : imgs) {
			imgTd.appendChild(productImg);
			if (idx > 1 && idx % 2 == 0) {
				Br br = new Br();
				imgTd.appendChild(br);
			}
			idx++;
		}
		return imgTd;
	}

	protected List<Img> getImgs(List<String> imgPathes, int width) {
		List<Img> imgs = new ArrayList<Img>();
		String widthStr = String.valueOf(width) + "px";
		for (String productImgPath : imgPathes) {
			Img productImg = getImg(widthStr, productImgPath);
			imgs.add(productImg);
		}
		return imgs;
	}

	private Img getImg(String widthStr, String productImgPath) {
		String productImage = productImgPath;
		Img productImg = new Img("productImg", productImage);
		productImg.setWidth(widthStr);
		return productImg;
	}

	/**
	 * generates HTML anchor link for entity resource
	 * @param entity
	 * @return entity link
	 */
	public static String entityToLink(Entity entity) {
		StringBuilder builder = new StringBuilder();
		if (entity.getUri().isEmpty()) {
			Span val = new Span();
			val.appendText("NULL");
			val.setAttribute("style", "color: red");
			builder.append(val.write());
		}
		if (!entity.isMeta()) {
			A link = new A(EntityDAO.getLongUri(configurator.getNamespacer(), entity));
			link.appendText(entity.toString());
			builder.append(link.write());
		} else {
			builder.append(entity.getUri().replace(":meta", ""));
		}
		return builder.toString();
	}

}
