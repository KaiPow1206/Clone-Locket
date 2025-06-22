import { Sequelize } from "sequelize";
import configDb from '../config/db.js';
const sequelize = new Sequelize(
    configDb.database,//ten database
    configDb.user,//ten user
    configDb.pass,//password
    {
        host: configDb.host,
        port: configDb.port,
        dialect: configDb.dialect,
    }
);

export default sequelize;