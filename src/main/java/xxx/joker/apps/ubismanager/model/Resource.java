package xxx.joker.apps.ubismanager.model;

import static xxx.joker.libs.javalibs.utils.JkStrings.strf;

/**
 * Created by f.barbano on 29/03/2018.
 */
public class Resource {

	private String userID;
	private String firstName;
	private String lastName;
	private String activity;
	private String email;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Resource)) return false;

		Resource resource = (Resource) o;

		return userID != null ? userID.equals(resource.userID) : resource.userID == null;
	}

	@Override
	public int hashCode() {
		return userID != null ? userID.hashCode() : 0;
	}

	@Override
	public String toString() {
		return String.format("%s - %s", userID, getFullName());
	}

	public String getFullName() {
		return strf("%s %s", lastName, firstName);
	}
	


	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
