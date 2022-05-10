package cokr.xit.ci.api.domain.repository;

import cokr.xit.ci.api.domain.NiceCiSymkeyMng;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NiceCiSymkeyMngRepository extends JpaRepository<NiceCiSymkeyMng, Long> {

    Optional<NiceCiSymkeyMng> findByPubkey(String pubkey);
}
