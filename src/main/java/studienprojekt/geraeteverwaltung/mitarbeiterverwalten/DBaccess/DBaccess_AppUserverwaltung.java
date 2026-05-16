package studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studienprojekt.geraeteverwaltung.mitarbeiterverwalten.DBaccess.entity.AppUser;

@Service
@Transactional
public class DBaccess_AppUserverwaltung {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public AppUser sucheNachUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username darf nicht leer sein");
        }

        List<AppUser> treffer = em.createQuery(
                        "SELECT a FROM AppUser a WHERE a.username = :username",
                        AppUser.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList();

        return treffer.isEmpty() ? null : treffer.getFirst();
    }
}
