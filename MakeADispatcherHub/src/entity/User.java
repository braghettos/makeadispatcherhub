package entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@XmlRootElement
@AllArgsConstructor
@NoArgsConstructor
public class User {
	@XmlElement
	private String id;
}
