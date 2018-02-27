package br.com.virtualsistemas.asteriskclient.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity
@Table(name="receiver")
@NamedQueries({
	@NamedQuery(name=Receiver.FIND_BY_URL,query="Select r from Receiver r where r.url = :url"),
	@NamedQuery(name=Receiver.FIND_BY_IDENTITY,query="Select r from Receiver r where r.identity = :identity")
})
public class Receiver implements Serializable{
	
	private static final long serialVersionUID = -34480465072171248L;
	public static final String FIND_BY_URL = "Receiver.findByUrl";
	public static final String FIND_BY_IDENTITY = "Receiver.findByIdentity";
	

	@Id
	@Column(unique=true)
	private String identity;
	
	private String description;
	
	
	@ElementCollection(fetch=FetchType.EAGER)
	private List<String> events;
	
	private String url;
	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<String> getEvents() {
		return events;
	}
	
	public void setEvents(List<String> events) {
		this.events = events;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((identity == null) ? 0 : identity.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		Receiver other = (Receiver) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (identity == null) {
			if (other.identity != null)
				return false;
		} else if (!identity.equals(other.identity))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}
