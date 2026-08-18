package com.travel.tour.repository.specification;

import com.travel.tour.constant.TourStatus;
import com.travel.tour.dto.TourSearchCriteria;
import com.travel.tour.entity.DestinationEntity;
import com.travel.tour.entity.TourEntity;
import com.travel.tour.entity.TourScheduleEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TourSpecification {

    private TourSpecification() {
    }

    public static Specification<TourEntity> filterByCriteria(TourSearchCriteria criteria) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ hiển thị các Tour đang ở trạng thái mở bán (PUBLISHED)
            predicates.add(cb.equal(root.get("status"), TourStatus.PUBLISHED));

            // Lọc theo từ khóa tìm kiếm (title, code, description)
            if (criteria.keyword() != null && !criteria.keyword().trim().isEmpty()) {
                String pattern = "%" + criteria.keyword().trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate codeLike = cb.like(cb.lower(root.get("code")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleLike, codeLike, descLike));
            }

            // Lọc theo điểm đến (thành phố hoặc tên điểm đến)
            if (criteria.destination() != null && !criteria.destination().trim().isEmpty()) {
                String destPattern = "%" + criteria.destination().trim().toLowerCase() + "%";
                Join<TourEntity, DestinationEntity> destJoin = root.join("destinations", JoinType.LEFT);
                Predicate cityLike = cb.like(cb.lower(destJoin.get("city")), destPattern);
                Predicate nameLike = cb.like(cb.lower(destJoin.get("name")), destPattern);
                predicates.add(cb.or(cityLike, nameLike));
            }

            // Lọc theo khoảng giá (minPrice, maxPrice)
            if (criteria.minPrice() != null || criteria.maxPrice() != null) {
                Join<TourEntity, TourScheduleEntity> schedJoin = root.join("schedules", JoinType.LEFT);
                if (criteria.minPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(schedJoin.get("priceAdult"), criteria.minPrice()));
                }
                if (criteria.maxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(schedJoin.get("priceAdult"), criteria.maxPrice()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
