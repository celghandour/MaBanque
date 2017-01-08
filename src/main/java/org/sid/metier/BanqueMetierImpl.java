package org.sid.metier;

import java.util.Date;

import org.sid.dao.CompteRepository;
import org.sid.dao.OperationRepository;
import org.sid.entities.Compte;
import org.sid.entities.CompteCourant;
import org.sid.entities.Operation;
import org.sid.entities.Retrait;
import org.sid.entities.Versement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class BanqueMetierImpl  implements IBanqueMetier{
	
	@Autowired
	private CompteRepository compteRepository ;
	@Autowired
	private OperationRepository OperationRepository;

	@Override
	public Compte consulterCompte(String codeCpte) {
	
		Compte cp = compteRepository.findOne(codeCpte);
		
		if (cp == null) throw new RuntimeException("Compte introuvable");
		
		return cp;
	}

	@Override
	public void verser(String codeCpte, double montant) {
		Compte cp = consulterCompte(codeCpte);
		
		Versement v = new Versement(new Date(), montant, cp);
		
		OperationRepository.save(v);
		
		cp.setSolde(cp.getSolde()+montant);
		compteRepository.save(cp);
	}

	@Override
	public void retirer(String codeCpte, double montant) {
		
		
				Compte cp = consulterCompte(codeCpte);
				
				double facilitesCaisse = 0 ;
				
				if (cp instanceof CompteCourant)
					facilitesCaisse = ((CompteCourant) cp).getDecouvert();
				
				if (cp.getSolde() + facilitesCaisse < montant )
					throw new RuntimeException("Solde insuffisant") ;
					
				Retrait r = new Retrait(new Date(), montant, cp);
						
						OperationRepository.save(r);
						
						cp.setSolde(cp.getSolde()-montant);
						compteRepository.save(cp);
		
	}

	@Override
	public void virement(String codeCpte1, String codeCpte2, double montant) {
		
		retirer(codeCpte1,montant);
		verser(codeCpte2, montant);
		
	}

	@Override
	public Page<Operation> ListOperation(String codeCpte, int page, int size) {
	
		
		return OperationRepository.ListOperation(codeCpte, new PageRequest(page, size));
	}

}
