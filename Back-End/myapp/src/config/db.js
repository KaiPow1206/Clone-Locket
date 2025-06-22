import dotenv from "dotenv";

dotenv.config();

export default {
   user: process.env.DB_USER,
   pass: process.env.DB_PASS,
   host: process.env.DB_HOST,
   database: process.env.DB_DATABASE,
   dialect: process.env.DB_DIALECT,
   port: process.env.DB_PORT
}