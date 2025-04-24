import { Box, Typography, Button, Container, Skeleton } from '@mui/material';
import { Swiper, SwiperSlide } from 'swiper/react';
import 'swiper/css';
import 'swiper/css/navigation';
import { Navigation, Autoplay } from 'swiper/modules';
import { useQuery } from '@tanstack/react-query';
import axios from '../api/axios';
import VinylCard from '../components/VinylCard';
import Footer from '../components/Footer';

export default function HomePage() {
  const { data: newVinyls, isLoading: loadingNew } = useQuery({
    queryKey: ['newVinyls2025'],
    queryFn: () => axios.get('/api/vinyls/new?limit=10').then(res => res.data)
  });

  const { data: randomVinyls, isLoading: loadingRandom } = useQuery({
    queryKey: ['randomVinyls'],
    queryFn: () => axios.get('/api/vinyls/random?limit=10').then(res => res.data)
  });

  const renderVinylSlides = (vinyls: any[], loading: boolean) => {
    if (loading) {
      return Array.from({ length: 4 }).map((_, idx) => (
        <SwiperSlide key={idx}>
          <Skeleton variant="rectangular" width={200} height={250} sx={{ bgcolor: '#333', borderRadius: 2 }} />
        </SwiperSlide>
      ));
    }

    if (!vinyls || vinyls.length === 0) {
      return (
        <Typography sx={{ color: '#ccc', mt: 2 }}>Нет доступных пластинок.</Typography>
      );
    }

    return vinyls.map((vinyl) => (
      <SwiperSlide key={vinyl.id}>
        <VinylCard vinyl={vinyl} />
      </SwiperSlide>
    ));
  };

  return (
    <>
      <Container sx={{ mt: 4 }}>
        <Typography variant="h5" gutterBottom sx={{ color: '#7cf152', textShadow: '0 0 5px #00e5ff'}}>
          Новые пластинки 2025
        </Typography>
        <Swiper
          modules={[Navigation, Autoplay]}
          navigation
          autoplay={{
            delay: 3000,
            disableOnInteraction: false,
            pauseOnMouseEnter: true,
          }}
          spaceBetween={20}
          loop
          style={{ padding: '15px 15px 15px 15px', position: 'relative', height: '350px', border:'rgba(0,229,255,0.3) 2px solid'}}
          breakpoints={{
            320: { slidesPerView: 1 },
            600: { slidesPerView: 2 },
            900: { slidesPerView: 3 },
            1200: { slidesPerView: 4 },
          }}
        >
          {renderVinylSlides(newVinyls, loadingNew)}
        </Swiper>


        <Typography variant="h5" gutterBottom sx={{ mt: 6, color: '#00e5ff', textShadow: '0 0 5px #00e5ff'}}>
          Случайные пластинки
        </Typography>
        <Swiper
          modules={[Navigation, Autoplay]}
          navigation
          autoplay={{
            delay: 3000,
            disableOnInteraction: false,
            pauseOnMouseEnter: true,
          }}
          spaceBetween={20}
          loop
          style={{ padding: '15px 15px 15px 15px', position: 'relative', height: '350px', border:'rgba(0,229,255,0.3) 2px solid'}}
          breakpoints={{
            320: { slidesPerView: 1 },
            600: { slidesPerView: 2 },
            900: { slidesPerView: 3 },
            1200: { slidesPerView: 4 },
          }}
        >
          {renderVinylSlides(randomVinyls , loadingRandom)}
        </Swiper>


        <Box
          sx={{
            mt: 8,
            p: 4,
            textAlign: 'center',
            background: 'linear-gradient(135deg, #1f1f1f 0%, #0d0d0d 100%)',
            borderRadius: 4,
            boxShadow: '0 0 20px rgba(0,255,255,0.1)',
            transition: 'transform 0.3s ease',
            '&:hover': {
              transform: 'translateY(-5px)',
              boxShadow: '0 0 30px rgba(0,255,255,0.2)',
            }
          }}
        >
          <Typography variant="h5" gutterBottom>
            Получите больше с подпиской!
          </Typography>
          <Typography variant="body1" sx={{ mb: 2 }}>
            Расширенный доступ к коллекциям, персональные рекомендации и многое другое.
          </Typography>
          <Button variant="contained" color="success" href="/subscription">
            Подробнее о подписке
          </Button>
        </Box>
      </Container>

      <Footer />
    </>
  );
}
