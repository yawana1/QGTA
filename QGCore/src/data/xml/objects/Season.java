package data.xml.objects;


public class Season {

	private String seasonName;
	private String seasonId;

	public String getSeasonName() {
		return seasonName;
	}
	public void setSeasonName(String seasonName) {
		this.seasonName = seasonName;
	}
	
	public String getSeasonId() {
		return seasonId;
	}
	public void setSeasonId(String seasonId) {
		this.seasonId = seasonId;
	}
	
	public Season(String seasonName, String seasonId) {
		this.seasonName = seasonName;
		this.seasonId = seasonId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((seasonId == null) ? 0 : seasonId.hashCode());
		result = prime * result
				+ ((seasonName == null) ? 0 : seasonName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Season other = (Season) obj;
		if (seasonId == null) {
			if (other.seasonId != null)
				return false;
		} else if (!seasonId.equals(other.seasonId))
			return false;
		if (seasonName == null) {
			if (other.seasonName != null)
				return false;
		} else if (!seasonName.equals(other.seasonName))
			return false;
		return true;
	}
}
