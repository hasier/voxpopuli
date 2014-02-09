package es.deusto.open311bilbao;

public enum Role {

	USER(3, 1), ADMIN(3, 1), OFFICIAL(5, 3);

	private int vote;
	private int base;

	private Role(int base, int vote) {
		this.base = base;
		this.vote = vote;
	}

	public int getVote() {
		return this.vote;
	}

	public int getBase() {
		return base;
	}

}
