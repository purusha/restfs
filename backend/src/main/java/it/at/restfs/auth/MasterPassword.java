package it.at.restfs.auth;

import java.util.UUID;

public class MasterPassword implements AuthorizationChecker {

	@Override
	public boolean isTokenValid(UUID container, String authorization) {
		return Boolean.TRUE;
	}
	
	/*

			ci deve essere un endpoint http che possa genarare un token per il Container
			
			POST /token ( su HttpListsener )


			se il container è configurato come MASTER_PWD 
			
				> l'auth header deve contenere la master password della configurazione
				> il token generato deve essere conservato su file ( UUID ) e ritornato al client ( durata ?, num di token generabili ? )
			
			
			se il container è configurato come NO_AUTH l'endpoint andrà in errore
			
				> 
			
			
			se il container è configurato come OAUTH2 ... ???
			
				> 
			
			
			
			Questa classe si occuperà di verificare l'esistenza del dato authorization nel file del container


	 */

}
