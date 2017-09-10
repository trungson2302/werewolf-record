package com.furiousrocket.soccho.werewolfrecord;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by trung on 9/8/2017.
 */

public class Season_Person {
  private String name;
  private String id;
  private int sum;
  private int win;
  private int wolf;
  private int villager;

  public Season_Person(){

  }

  public Season_Person(String id,String name){
    this.id=id;sum=0;win=0;wolf=0;villager=0;
    this.name=name;
  }
  public Season_Person(String id,String name, int sum, int win, int wolf, int villager) {
    this.id = id;
    this.sum = sum;
    this.name=name;
    this.win = win;
    this.wolf = wolf;
    this.villager = villager;
  }

  public Map<String, Object> toMap() {
    HashMap<String, Object> result = new HashMap<>();
    result.put("name", name);
    result.put("id", id);
    result.put("sum", sum);
    result.put("win", win);
    result.put("wolf", wolf);
    result.put("villager", villager);
    return result;
  }
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getSum() {
    return sum;
  }

  public void setSum(int sum) {
    this.sum = sum;
  }

  public int getWin() {
    return win;
  }

  public void setWin(int win) {
    this.win = win;
  }

  public int getWolf() {
    return wolf;
  }

  public void setWolf(int wolf) {
    this.wolf = wolf;
  }

  public int getVillager() {
    return villager;
  }

  public void setVillager(int villager) {
    this.villager = villager;
  }
}
