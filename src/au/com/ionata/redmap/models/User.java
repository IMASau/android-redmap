package au.com.ionata.redmap.models;

import au.com.ionata.redmap.api.models.UserDetailPrim;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User {
	
	@DatabaseField(generatedId=true)
	public int Id;
	
	@DatabaseField(canBeNull=true)
    public Integer Pk = null;
	
	@DatabaseField
	public String Username;

	@DatabaseField
	public String NewPassword;
	
	@DatabaseField
	public String Email;

	@DatabaseField
	public String FirstName;

	@DatabaseField
	public String LastName;

	@DatabaseField(canBeNull=true)
	public Boolean JoinMailingList = null;

	@DatabaseField(foreign=true, canBeNull=true, foreignAutoRefresh=true, maxForeignAutoRefreshLevel=3)
    public Region Region = null;

	@DatabaseField
    public int Status;

	@DatabaseField(canBeNull=true)
	public String AuthToken;

	@DatabaseField(canBeNull=true)
	public String FacebookToken;

	@DatabaseField
	public boolean LoggedIn = false;
	
    public User(){
    	super();
    }

    public User(UserDetailPrim userPrim, String token){
        super();
        Pk = userPrim.id;
        Username = userPrim.username;
        Email = userPrim.email;
        FirstName = userPrim.first_name;
        LastName = userPrim.last_name;
        AuthToken = token;
    }
    
    public User(String username, String password, String firstName, String lastName, String email, boolean joinMailing, Region region){
        super();
        Username = username;
        NewPassword = password;
        FirstName = firstName;
        LastName = lastName;
        Email = email;
        JoinMailingList = joinMailing;
        Region = region;
    }

    @Override
    public boolean equals(Object object) {
    	if (object.getClass() == User.class){
    		return ((User)object).Id == Id;
    	}

        return false;
    }

    @Override
    public int hashCode() {
        return Id;
    }

    public String toJson(){
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(new PostPrimitive(this));
		return json;
	}

    public class PostPrimitive {
		public String username;
		public String password;
		public String email;
		public String first_name;
		public String last_name;
		public Boolean join_mailing_list = true;
		public String region = "";

		public PostPrimitive(User user){
			this.username = user.Username;
			this.password = user.NewPassword;
			this.email = user.Email;
			this.first_name = user.FirstName;
			this.last_name = user.LastName;
			this.join_mailing_list = user.JoinMailingList ? true : false;
			if (user.Region != null){
			    this.region = user.Region.Description;
			}
		}
	}

	public void Update(UserDetailPrim userPrim)
    {
        Username = userPrim.username;
        FirstName = userPrim.first_name;
        LastName = userPrim.last_name;
        Email = userPrim.email;
    }
}
