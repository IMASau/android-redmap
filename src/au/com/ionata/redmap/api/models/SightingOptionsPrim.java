package au.com.ionata.redmap.api.models;

import java.util.List;

public class SightingOptionsPrim {
	public String response;
	public List<Count> count;
	public List<Organisation> organisation;
	public List<Habitat> habitat;
	public List<Region> region;
	public List<Activity> activity;
	public List<Sex> sex;
	public List<WeightMethod> weight_method;
	public List<SizeMethod> size_method;
	public List<Time> time;
	public List<Method> method;
	public List<Accuracy> accuracy;
	
	public abstract class Model{
		public int pk;
		public String model;
	}
	
	public class Count extends Model {
		public Fields fields;
		
		public class Fields {
			public int update_number;
			public String code;
			public String description;
		}
	}
	
	public class Organisation extends Model {
		public Fields fields;
		
		public class Fields {
			public String description;
		}
	}
	
	public class Habitat extends Model {
		public Fields fields;
		
		public class Fields {
			public int update_number;
			public String code;
			public String description;
		}
	}
	
	public class Region extends Model {
		public Fields fields;
		
		public class Fields {
			public String jurisdiction;
			public String description;
			public String slug;
		}
	}

	public class Activity extends Model {
		public Fields fields;
		
		public class Fields {
			public int update_number;
			public String code;
			public String description;
		}
		
		/*public String toString(){
			return fields.description;
		}*/
	}
	
	public class Sex extends Model {
		public Fields fields;
		
		public class Fields {
			public int update_number;
			public String code;
			public String description;
		}
	}

	public class WeightMethod extends Model {
		public Fields fields;
		
		public class Fields {
			public int update_number;
			public String code;
			public String description;
		}
	}

	public class SizeMethod extends Model {
		public Fields fields;
		
		public class Fields {
			public int update_number;
			public String code;
			public String description;
		}
	}
	
	public class Time extends Model {
		public Fields fields;
		
		public class Fields {
			public int update_number;
			public String code;
			public String description;
		}
	}
	
	public class Method extends Model {
	}

	public class Accuracy extends Model {
		public Fields fields;
		
		public class Fields {
			public int update_number;
			public String code;
			public String description;
		}
	}

	public Count getCountById(int id){
		Count count;
		for (int i = 0; i < this.count.size(); i++) {
			count = this.count.get(i);
			if (count.pk == id) return count;
		}
		return null;
	}

	public Habitat getHabitatById(int id){
		Habitat habitat;
		for (int i = 0; i < this.habitat.size(); i++) {
			habitat = this.habitat.get(i);
			if (habitat.pk == id) return habitat;
		}
		return null;
	}
	
	public Activity getActivityById(int id){
		Activity activity;
		for (int i = 0; i < this.activity.size(); i++) {
			activity = this.activity.get(i);
			if (activity.pk == id) return activity;
		}
		return null;
	}

	
	public Sex getSexById(int id){
		Sex sex;
		for (int i = 0; i < this.sex.size(); i++) {
			sex = this.sex.get(i);
			if (sex.pk == id) return sex;
		}
		return null;
	}

	
	public WeightMethod getWeightMethodById(int id){
		WeightMethod weightMethod;
		for (int i = 0; i < this.weight_method.size(); i++) {
			weightMethod = this.weight_method.get(i);
			if (weightMethod.pk == id) return weightMethod;
		}
		return null;
	}
	
	
	public SizeMethod getSizeMethodById(int id){
		SizeMethod sizeMethod;
		for (int i = 0; i < this.size_method.size(); i++) {
			sizeMethod = this.size_method.get(i);
			if (sizeMethod.pk == id) return sizeMethod;
		}
		return null;
	}
	
	public Time getTimeById(int id){
		Time time;
		for (int i = 0; i < this.time.size(); i++) {
			time = this.time.get(i);
			if (time.pk == id) return time;
		}
		return null;
	}
	
	public Time getTimeByHour(int hour){
		Time time;
		for (int i = 0; i < this.time.size(); i++) {
			time = this.time.get(i);
			if (time.fields.code != "NS" && Integer.valueOf(time.fields.code) == hour) return time;
		}
		return null;
	}
	
	public Accuracy getAccuracyById(int id){
		Accuracy accuracy;
		for (int i = 0; i < this.accuracy.size(); i++) {
			accuracy = this.accuracy.get(i);
			if (accuracy.pk == id) return accuracy;
		}
		return null;
	}
}
