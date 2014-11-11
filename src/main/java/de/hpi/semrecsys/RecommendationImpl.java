package de.hpi.semrecsys;

import javax.persistence.*;

import de.hpi.semrecsys.model.Product;
import de.hpi.semrecsys.persistence.ProductDAO;
import de.hpi.semrecsys.utils.StringUtils;

import java.util.Date;

@Entity
@Table(name = "recommendation")
public abstract class RecommendationImpl implements DBObject {

    private RecommendationId id;
    protected String type;
    protected Double score;
    protected Double relativeScore = 0.0;
    protected Date createdAt;

    public RecommendationImpl() {
        super();
    }

    public RecommendationImpl(RecommendationId id) {
        this.id = id;
    }


    public RecommendationImpl(RecommendationId id, String type) {
        this.id = id;
        this.type = type;
    }

    public RecommendationImpl(RecommendationId id, String type, Double score,
                          Double relativeScore, Date createdAt) {
        this.id = id;
        this.type = type;
        this.score = score;
        this.relativeScore = relativeScore;
        this.createdAt = createdAt;
    }

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "productId", column = @Column(name = "product_id", nullable = false)),
            @AttributeOverride(name = "dtype", column = @Column(name = "DTYPE", nullable = false)),
            @AttributeOverride(name = "position", column = @Column(name = "position", nullable = false)),
            @AttributeOverride(name = "linkedProductId", column = @Column(name = "linked_product_id", nullable = false)) })
    public RecommendationId getId() {
        return this.id;
    }

    public void setId(RecommendationId id) {
        this.id = id;
    }

    @Column(name = "type", nullable = false, length = 32)
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "score", precision = 22, scale = 0)
    public Double getScore() {
        return this.score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Column(name = "relative_score", precision = 22, scale = 0)
    public Double getRelativeScore() {
        return this.relativeScore;
    }

    public void setRelativeScore(Double relativeScore) {
        this.relativeScore = relativeScore;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", length = 19)
    public Date getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        String result = getType() + " " + id.getPosition() + " : " + recommendedProduct()
                + recommendationScoreToString() + "\n";
        return result;
    }

    public Product recommendedProduct() {
        return ProductDAO.getDefault().findById(getId().getLinkedProductId());
    }


    public String recommendationScoreToString() {

        String result = " ";
        if (score != null && relativeScore != null) {
            result = "(score = " + StringUtils.doubleToString(score) + "; relative = "
                    + StringUtils.doubleToString(relativeScore) + ")";
        }
        return result;
    }


    public boolean empty(){
        return recommendedProduct() == null || recommendedProduct().getTitle().isEmpty();
    }


}