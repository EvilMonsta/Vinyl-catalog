import { useNavigate, Link } from 'react-router-dom';
import { Box, Button, TextField, Typography } from '@mui/material';
import { useForm } from 'react-hook-form';
import { useMutation } from '@tanstack/react-query';
import { login } from '../api/auth';
import {useEffect, useState} from 'react';

export default function LoginPage() {
  const { register, handleSubmit, reset } = useForm();
  const navigate = useNavigate();
  const [error, setError] = useState('');

  useEffect(() => {
    reset({
      username: '',
      email: '',
      password: ''
    });
  }, []);

  const mutation = useMutation({
    mutationFn: login,
onSuccess: (data) => {
  if (!data?.token) {
    console.error('Token is missing in response:', data);
    setError('Ошибка авторизации: не получен токен');
    return;
  }

  const token = data.token;
  localStorage.setItem('token', token);

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    localStorage.setItem('username', payload.username);
    localStorage.setItem('role', payload.role);
  } catch (e) {
    console.error('Ошибка при разборе токена:', e);
    setError('Ошибка при обработке токена. Попробуйте снова.');
    return;
  }

  navigate('/');
  window.location.reload();
}
,
    onError: () => setError('Неверный email или пароль'),
  });

  const onSubmit = (data: any) => {
    setError('');
    mutation.mutate(data);
  };

  return (
    <Box maxWidth={400} mx="auto" mt={8}>
      <Typography variant="h5" gutterBottom>Вход</Typography>
      <form onSubmit={handleSubmit(onSubmit)}>
        <TextField fullWidth margin="normal" label="Email" {...register('email')} />
        <TextField fullWidth margin="normal" type="password" label="Пароль" {...register('password')} />
        {error && <Typography color="error">{error}</Typography>}
        <Button type="submit" fullWidth variant="contained" sx={{ mt: 2 }}>Войти</Button>
      </form>
      <Typography variant="body2" sx={{ mt: 2 }}>
        Впервые на сайте? <Link to="/register">Зарегистрируйтесь</Link>
      </Typography>
    </Box>
  );
}
