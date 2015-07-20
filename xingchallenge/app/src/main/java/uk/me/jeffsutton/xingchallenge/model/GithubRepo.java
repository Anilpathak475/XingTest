package uk.me.jeffsutton.xingchallenge.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model to represent a single GitHub repository
 * <p/>
 * Created by jeffsutton on 20/07/15.
 */
public class GithubRepo {

    public int id;
    public String name;
    public String full_name;
    public GithubUser owner;
    @SerializedName("private")
    public boolean private_repo;
    public String html_url;
    public String description;
    public Boolean fork;
}
