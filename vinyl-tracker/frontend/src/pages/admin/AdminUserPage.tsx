import {
  Box, Typography, Table, TableBody, TableCell, TableHead, TableRow,
  Paper, Select, MenuItem, Dialog, DialogActions, DialogContent,
  DialogContentText, DialogTitle, Button, Divider, TableContainer
} from '@mui/material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import axios from '../../api/axios';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const roleMap: Record<number, string> = {
  1: 'USER',
  2: 'VIP_USER',
  3: 'ADMIN'
};

const reverseRoleMap: Record<string, number> = {
  'USER': 1,
  'VIP_USER': 2,
  'ADMIN': 3
};

interface User {
  id: number;
  username: string;
  email: string;
  roleId: number;
}

export default function AdminUsersPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [newRole, setNewRole] = useState('');
  const [confirmOpen, setConfirmOpen] = useState(false);

  const { data: users = [], isLoading } = useQuery<User[]>({
    queryKey: ['adminUsers'],
    queryFn: () =>
      axios.get('/api/admin/users', {
        headers: { Authorization: `Bearer ${auth?.user?.token}` },
      }).then(res => res.data),
    enabled: !!auth?.user && auth.user.role === 'ADMIN',
  });

  const mutation = useMutation({
    mutationFn: ({ id, roleId }: { id: number, roleId: number }) =>
      axios.put(`/api/admin/users/update-role/${id}`,
        { roleId },
        {
          headers: {
            Authorization: `Bearer ${auth?.user?.token}`,
          },
        }
      ),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['adminUsers'] }),
  });

  const handleChangeRole = (id: number, currentRole: string, selected: string) => {
    if (currentRole !== selected) {
      setSelectedUserId(id);
      setNewRole(selected);
      setConfirmOpen(true);
    }
  };

  const handleConfirm = () => {
    if (selectedUserId !== null) {
      mutation.mutate({ id: selectedUserId, roleId: reverseRoleMap[newRole] });
    }
    setConfirmOpen(false);
  };

  if (!auth?.user || auth.user.role !== 'ADMIN') {
    navigate('/');
    return null;
  }

  if (isLoading) return <Typography>Загрузка пользователей...</Typography>;

  return (
    <Box sx={{ p: 4 }}>
      <Typography variant="h4" gutterBottom sx={{ color: '#00e5ff', textShadow: '0 0 8px #00e5ff'}}>
        Панель администратора — Пользователи
      </Typography>

      <TableContainer component={Paper} sx={{ backgroundColor: '#1f1f1f', mt: 2 }}>
        <Table size="small">
          <TableHead className="neon-header">
            <TableRow>
              <TableCell>Имя пользователя</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Роль</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((user) => (
              <TableRow key={user.id} sx={{ height: 40 }}>
                <TableCell sx={{ py: 0.5 }}>{user.username}</TableCell>
                <TableCell sx={{ py: 0.5 }}>{user.email}</TableCell>
                <TableCell sx={{ py: 0.5 }}>
                  <Select
                    value={roleMap[user.roleId]}
                    onChange={(e) => handleChangeRole(user.id, roleMap[user.roleId], e.target.value)}
                    className="neon-select neon-glow"
                    sx={{
                      fontSize: '0.8rem',
                      width: 120,
                      backgroundColor: '#2a2a2a',
                      borderRadius: '8px',
                      '& .MuiSelect-icon': { color: '#6ee7ff' },
                    }}
                  >
                    <MenuItem value="USER">USER</MenuItem>
                    <MenuItem value="VIP_USER">VIP_USER</MenuItem>
                    <MenuItem value="ADMIN">ADMIN</MenuItem>
                  </Select>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle sx={{ color: '#00e5ff' }}>Подтверждение изменения роли</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Вы уверены, что хотите изменить роль пользователя на <strong style={{ color: '#00ffcc' }}>{newRole}</strong>?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Отмена</Button>
          <Button onClick={handleConfirm} variant="contained" color="success">
            Подтвердить
          </Button>
        </DialogActions>
      </Dialog>

      <Divider sx={{ my: 4, borderColor: '#00e5ff' }} />

      <Box>
        <Typography variant="h6" gutterBottom sx={{ color: '#6ee7ff' }}>
          Управление винилами
        </Typography>
        <Button
          variant="outlined"
          className="neon-glow"
          onClick={() => navigate('/admin/vinyls')}
        >
          Перейти к управлению винилами
        </Button>
      </Box>
    </Box>
  );
}
