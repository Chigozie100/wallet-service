package com.wayapaychat.temporalwallet.entity;

import lombok.*;
import javax.persistence.*;

@Data
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "m_channel_providers")
public class ChannelProvider {
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
@Column(unique = true, nullable = false)
private Long id;

private String name;

private boolean isActive;

}
