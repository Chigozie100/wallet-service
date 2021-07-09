package com.wayapaychat.temporalwallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Long id;


    @Column(unique = true, nullable = false)
    private Long userId;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Accounts> accounts;

    @JsonIgnore
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    private String firstName;
    
    private String lastName;
//    private String externalId;
    private int savingsProductId;
    
    @Column(unique = true, nullable = false)
    private String emailAddress;
    
    @Column(unique = true, nullable = false)
    private String mobileNo;

	public Users(Long userId, List<Accounts> accounts, Date createdAt, String firstName, String lastName,
			int savingsProductId, String emailAddress, String mobileNo) {
		super();
		this.userId = userId;
		this.accounts = accounts;
		this.createdAt = createdAt;
		this.firstName = firstName;
		this.lastName = lastName;
		this.savingsProductId = savingsProductId;
		this.emailAddress = emailAddress;
		this.mobileNo = mobileNo;
	}

	
    

}
