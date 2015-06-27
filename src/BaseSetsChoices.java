

/*  
  "version": 1,
  "owner": "me",
  "name": "choices for me",
  "update": false,
  "inventorysets": [
    {
      "name": "highrollers",
      "creteria": {
        "state": [
          "NY",
          "NJ"
        ],
        "income": [
          "affluent",
          "middle"
        ],
        "gender": [
          "M"
        ],
        "content": [
          "business",
          "sport",
          "food",
          "news"
        ],
        "age": [
          "middle",
          "young",
          "child"
        ]
      },
      "goal": 150000
    }
  ]
}
*/
public class BaseSetsChoices {
	int version;
	String owner;
	String name;
	boolean update;
	
	BaseSet[] inventorysets;
}
