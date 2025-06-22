import sequelize from '../config/connect.js';
import { createRefToken, createToken, verifyRefToken } from '../config/jwt.js';
import initModels from "../models/init-models.js";
import bcrypt from 'bcrypt';

const model = initModels(sequelize);

const loginAccount = async (req, res) => {
   try {
      const { username, password } = req.body;

      if (!username || !password) {
         return res.status(400).json({ message: "Please enter your username or password" });
      }

      const user = await model.users.findOne({ where: { username } });

      if (!user) {
         return res.status(404).json({ message: "User not found" });
      }

      const isPasswordValid = bcrypt.compareSync(password, user.password_hash);

      if (!isPasswordValid) {
         return res.status(401).json({ message: "Invalid password" });
      }

      const { password_hash, ...userData } = user.dataValues;
      let accessToken = createToken({ id: user.id });
      let refreshToken = createRefToken({ id: user.id });
      await model.users.update({
         refresh_token: refreshToken
      }, {
         where: { id: user.id }
      });
      return res.status(200).json({
         message: "login successfully",
         data: accessToken,
         refreshToken: refreshToken,
      });
   } catch (error) {
      console.log(error)
      res.status(500).json({ message: "Internal Server Error" });
   }
};

const signUpAccount = async (req, res) => {
   try {
      const { username, email, password } = req.body;

      if (!username || !email || !password) {
         return res.status(400).json({ message: "Please enter your username, email, and password" });
      }

      const existingUser = await model.users.findOne({ where: { username } });
      if (existingUser) {
         return res.status(409).json({ message: "Username already exists" });
      }

      const existingEmail = await model.users.findOne({ where: { email } });
      if (existingEmail) {
         return res.status(409).json({ message: "Email already exists" });
      }

      const hashedPassword = bcrypt.hashSync(password, 10);

      const newUser = await model.users.create({
         username,
         email,
         password_hash: hashedPassword
      });

      const { password_hash, ...userData } = newUser.dataValues;
      res.status(201).json(userData);
   } catch (error) {
      res.status(500).json({ message: "Internal Server Error" });
   }
};

const extendToken = async (req, res) => {
   try {
      const {refreshToken} = req.body;

      if (!refreshToken) {
         return res.status(401).json({ message: "Missing refresh token" });
      }

      // Kiểm tra refreshToken trong DB
      const user = await model.users.findOne({
         where: { refresh_token: refreshToken }
      });

      if (!user) {
         return res.status(403).json({ message: "Refresh token is invalid" });
      }

      // Verify refresh token
      const verified = verifyRefToken(refreshToken);

      const newAccessToken = createToken({ id: user.id });
      return res.status(200).json({
         message: "New access token generated successfully",
         data: newAccessToken
      });
   } catch (error) {
      console.error(error);
      return res.status(500).json({ message: "Internal server error" });
   }
};

const logoutAccount = async (req, res) => {
  try {
    const { refreshToken } = req.body;

    if (!refreshToken) {
      return res.status(400).json({ message: "Missing refresh token" });
    }

    // Tìm user có refresh token này
    const user = await model.users.findOne({
      where: { refresh_token: refreshToken }
    });

    if (!user) {
      return res.status(400).json({ message: "Invalid refresh token" });
    }

    // Xóa refresh token khỏi DB (đăng xuất)
    await model.users.update({
      refresh_token: null
    }, {
      where: { id: user.id }
    });

    return res.status(200).json({ message: "Logout successfully" });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: "Internal server error" });
  }
};

export {
   loginAccount,
   signUpAccount,
   extendToken,
   logoutAccount
};