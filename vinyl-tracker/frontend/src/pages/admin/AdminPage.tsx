import {
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Paper,
  Divider,
} from '@mui/material';
import { useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';

interface User {
  id: number;
  username: string;
  email: string;
  role: string;
}

export default function AdminPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const [users, setUsers] = useState<User[]>([]);

  useEffect(() => {
    if (!auth?.user) return;

    if (auth.user.role !== 'ADMIN') {
      navigate('/');
      return;
    }

    const loadUsers = async () => {
      try {
        const res = await axios.get('/api/admin/users', {
          headers: { Authorization: `Bearer ${auth.user!.token}` },
        });
        setUsers(res.data);
      } catch (err) {
        console.error('Ошибка загрузки пользователей:', err);
      }
    };

    loadUsers();
  }, [auth?.user, navigate]);


  return (
    <Box sx={{ p: 4 }}>
      <Typography variant="h4" gutterBottom color="primary">
        Панель администратора
      </Typography>

      <Typography variant="h6" sx={{ mt: 4 }}>
        Пользователи
      </Typography>
      <Paper sx={{ mt: 2 }}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Имя пользователя</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Роль</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((u) => (
              <TableRow key={u.id}>
                <TableCell>{u.username}</TableCell>
                <TableCell>{u.email}</TableCell>
                <TableCell>{u.role}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>

      <Divider sx={{ my: 4 }} />

      <Typography variant="h6">
        Управление винилами (в разработке)
      </Typography>
      <Box sx={{ mt: 2 }}>
        {/* Тут позже появится интерфейс для редактирования винилов */}
        <Typography variant="body2" color="text.secondary">
          Здесь будет API-интерфейс для добавления, редактирования и удаления винилов.
        </Typography>
      </Box>
    </Box>
  );
}
