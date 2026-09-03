package ru.alta.thirdproj.entites;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
//@Builder
@NoArgsConstructor
public class Role {

	private Long id;

	private String name;

	public static final Map<String, String> COLUMN_MAPPINGS = new HashMap<>();

	static {
		COLUMN_MAPPINGS.put("id", "id");
		COLUMN_MAPPINGS.put("name", "name");

	}

	public String toString() {
		return "Role{" + "id=" + id + ", name='" + name + '\'' + '}';
	}
}
