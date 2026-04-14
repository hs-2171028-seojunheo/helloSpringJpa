package kr.ac.hansung.cse.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import kr.ac.hansung.cse.model.Product;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * ProductRepository - 데이터 접근 계층 (Repository Layer)
 * =====================================================================
 *
 * Repository 패턴: 데이터 저장소(DB)에 대한 접근 로직을 캡슐화합니다.
 * Service 계층은 데이터가 어디서 오는지(DB, 캐시, 파일 등) 알 필요 없이
 * Repository 인터페이스만 사용합니다.
 *
 * @Repository : 다음 두 가지 역할을 합니다.
 *   1. @Component의 특수화 → Spring이 이 클래스를 빈으로 등록합니다.
 *   2. DataAccessException 변환기 활성화 →
 *      JDBC/JPA 예외를 Spring의 DataAccessException 계층으로 변환합니다.
 *      덕분에 서비스 계층이 특정 DB 기술의 예외에 의존하지 않아도 됩니다.
 *
 * [EntityManager 란?]
 * JPA의 핵심 인터페이스로 엔티티의 생명주기를 관리합니다.
 *
 * 엔티티 생명주기:
 *   Transient(비영속) → persist() → Managed(영속)
 *   Managed(영속)     → remove()  → Removed(삭제)
 *   Managed(영속)     → detach()  → Detached(준영속)
 *
 * [영속성 컨텍스트(Persistence Context)]
 * EntityManager가 관리하는 1차 캐시입니다.
 * 같은 트랜잭션 내에서 동일한 엔티티를 두 번 조회하면
 * DB가 아닌 영속성 컨텍스트에서 반환합니다.(1차 캐시)
 * 트랜잭션 종료 시 변경된 엔티티를 자동으로 DB에 반영합니다.(더티 체킹)
 */
@Repository
public class ProductRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Product> findAll() {
        // LEFT JOIN FETCH: LAZY인 category를 한 번의 쿼리로 함께 로드 (LazyInitializationException 방지)
        TypedQuery<Product> query = entityManager
                .createQuery("SELECT p FROM Product p LEFT JOIN FETCH p.category ORDER BY p.id ASC", Product.class);
        return query.getResultList();
    }

    public Optional<Product> findById(Long id) {
        // LEFT JOIN FETCH: em.find()는 JOIN FETCH 불가 → JPQL로 대체
        List<Product> result = entityManager
                .createQuery("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id", Product.class)
                .setParameter("id", id)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public Product save(Product product) {
        entityManager.persist(product);
        return product;
    }

    public List<Product> findByNameContaining(String keyword) {
        List<Product> product = entityManager.createQuery(
                "SELECT p FROM Product P WHERE p.name LIKE :keyword",
                Product.class)
                .setParameter("keyword", "%"+keyword+"%")
                .getResultList();
        return product;
    }

    public List<Product> findByCategoryId(Long categoryId) {
        List<Product> products = entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.category.id = :cid",
                Product.class)
                .setParameter("cid", categoryId)
                .getResultList();
        return products;
    }

    public Product update(Product product) {
        return entityManager.merge(product);
    }

    public void delete(Long id) {
        Product product = entityManager.find(Product.class, id);
        if (product != null) {
            entityManager.remove(product);
        }
    }
}
