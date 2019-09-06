package it.at.restfs.auth;

public class MasterPassword implements AuthorizationChecker {

	@Override
	public boolean isTokenValid(String authorization) {
		return Boolean.TRUE;
	}
	
	/*

			ci deve essere un endpoint http che possa genarare un token per il Container
			
			il token generato deve essere conservato su file ( UUID ) e ritornato al client ( durata ?, num di token generabili ? )
			
			questa classe si occuperà di verificare l'esistenza del dato authorization nel file del container


	 */

}
