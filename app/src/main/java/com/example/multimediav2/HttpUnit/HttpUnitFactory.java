package com.example.multimediav2.HttpUnit;

  public class HttpUnitFactory {

      static public IHttpUnit Get()
      {
          return new HttpUnit_Okhttp();
      }
}
