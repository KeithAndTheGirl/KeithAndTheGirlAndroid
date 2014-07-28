package com.keithandthegirl.app.db.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Created by dmfrey on 7/28/14.
 */
public class YoutubeEntry {

    @SerializedName( "author" )
    private YoutubeAuthor[] authors;

    @SerializedName( "category" )
    private YoutubeCategory[] categories;

    @SerializedName( "content" )
    private YoutubeContent content;

    @SerializedName( "gd$etag" )
    private String etag;

    @SerializedName( "generator" )
    private YoutubeGenerator generator;

    @SerializedName( "id" )
    private YoutubeValue id;

    @SerializedName( "link" )
    private YoutubeLink[] links;

    @SerializedName( "published" )
    private YoutubeDate published;

    @SerializedName( "title" )
    private YoutubeValue title;

    @SerializedName( "updated" )
    private YoutubeDate updated;

    public YoutubeEntry() { }

    public YoutubeAuthor[] getAuthors() {
        return authors;
    }

    public void setAuthors( YoutubeAuthor[] authors ) {

        this.authors = authors;

    }

    public YoutubeCategory[] getCategories() {
        return categories;
    }

    public void setCategories( YoutubeCategory[] categories ) {

        this.categories = categories;

    }

    public YoutubeContent getContent() {
        return content;
    }

    public void setContent( YoutubeContent content ) {

        this.content = content;

    }

    public String getEtag() {
        return etag;
    }

    public void setEtag( String etag ) {

        this.etag = etag;

    }

    public YoutubeGenerator getGenerator() {
        return generator;
    }

    public void setGenerator( YoutubeGenerator generator ) {

        this.generator = generator;

    }

    public YoutubeValue getId() {
        return id;
    }

    public void setId( YoutubeValue id ) {

        this.id = id;

    }

    public YoutubeLink[] getLinks() {
        return links;
    }

    public void setLinks( YoutubeLink[] links ) {

        this.links = links;

    }

    public YoutubeDate getPublished() {
        return published;
    }

    public void setPublished( YoutubeDate published ) {

        this.published = published;

    }

    public YoutubeValue getTitle() {
        return title;
    }

    public void setTitle( YoutubeValue title ) {

        this.title = title;

    }

    public YoutubeDate getUpdated() {
        return updated;
    }

    public void setUpdated( YoutubeDate updated ) {

        this.updated = updated;

    }

    @Override
    public String toString() {
        return "YoutubeEntry{" +
                "authors=" + Arrays.toString(authors) +
                ", category=" + Arrays.toString(categories) +
                ", etag='" + etag + '\'' +
                ", generator='" + generator + '\'' +
                ", id='" + id + '\'' +
                ", links=" + Arrays.toString(links) +
                ", published=" + published +
                ", title=" + title +
                ", updated=" + updated +
                '}';
    }

}
