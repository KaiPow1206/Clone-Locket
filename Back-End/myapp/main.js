import express from 'express';
import dotenv from 'dotenv';
import cors from 'cors';
import bodyParser from 'body-parser';
import rootRoutes from './src/routes/root.router.js';

dotenv.config();

const app = express();
const port = process.env.PORT;

app.use('/uploads', express.static('uploads'));
app.use(cors());
app.use(bodyParser.json());
app.use(rootRoutes);
app.listen(port, '0.0.0.0', () => {
  console.log(`Server is running at http://localhost:${port}`);
});
