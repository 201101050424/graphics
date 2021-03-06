package se.kth.livetech.contest.model.impl;

import java.util.Map;

import se.kth.livetech.contest.model.Judgement;

public class JudgementImpl extends EntityImpl implements Judgement {
	String acronym;

	JudgementImpl(Map<String, String> attrs) {
		super(attrs);
		// Some Judgement acronym heuristics...
		if (attrs.containsKey("acronym"))
			acronym = attrs.get("acronym");
		else if (id == 0 || "Yes".equalsIgnoreCase(name)) {
			acronym = "AC";
		} else {
			acronym = "";
			for (int i = 0; i < name.length(); ++i)
				if (Character.isUpperCase(name.charAt(i)))
					acronym += name.charAt(i);
			if (acronym.length() < 2)
				acronym = "J" + id;
		}
	}
	
	public String getType() {
		return "judgement";
	}

	public String getAcronym() {
		return acronym;
	}
}
