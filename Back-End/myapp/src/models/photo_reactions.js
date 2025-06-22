import _sequelize from 'sequelize';
const { Model, Sequelize } = _sequelize;

export default class photo_reactions extends Model {
  static init(sequelize, DataTypes) {
  return super.init({
    id: {
      autoIncrement: true,
      type: DataTypes.INTEGER,
      allowNull: false,
      primaryKey: true
    },
    photo_id: {
      type: DataTypes.INTEGER,
      allowNull: false,
      references: {
        model: 'photos',
        key: 'id'
      }
    },
    user_id: {
      type: DataTypes.INTEGER,
      allowNull: false,
      references: {
        model: 'users',
        key: 'id'
      }
    },
    type: {
      type: DataTypes.ENUM('love','haha','wow','sad'),
      allowNull: false
    }
  }, {
    sequelize,
    tableName: 'photo_reactions',
    timestamps: true,
    indexes: [
      {
        name: "PRIMARY",
        unique: true,
        using: "BTREE",
        fields: [
          { name: "id" },
        ]
      },
      {
        name: "photo_id",
        using: "BTREE",
        fields: [
          { name: "photo_id" },
        ]
      },
      {
        name: "user_id",
        using: "BTREE",
        fields: [
          { name: "user_id" },
        ]
      },
    ]
  });
  }
}
