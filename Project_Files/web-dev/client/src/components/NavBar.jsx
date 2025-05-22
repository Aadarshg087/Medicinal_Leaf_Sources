import React from "react";
import logo from "../assets/logo.png";

const NavBar = () => {
  return (
    <div className="w-full border">
      <div className=" flex flex-row justify-between p-6 bg-green-800 text-white">
        <h2 className=" text-3xl font-winky">Welcome to GreenCure</h2>
        <img src={logo} alt="Img" className="h-[40px] bg-amber-50" />
      </div>
    </div>
  );
};

export default NavBar;
