#![cfg(target_os = "android")]
#![allow(non_snake_case)]

use std::io;

use android_logger::{Config, FilterBuilder};
use chrono::Local;
use image::{error, ImageReader, Rgb, RgbImage};
use jni::objects::{JClass, JString};
use jni::JNIEnv;
use log::{debug, error, LevelFilter};

fn init_log() {
    android_logger::init_once(
        Config::default()
            .with_max_level(LevelFilter::Trace)
            .with_tag("rustlib")
            .with_filter(FilterBuilder::new().parse("debug").build()),
    );
}

fn create_image(dir: String, input: String) -> anyhow::Result<String> {
    let img = ImageReader::open(&input)?.decode()?;
    // let color_type = img.color();
    // let w = img.width();
    // let h = img.height();
    // let s = format!("{:?}, {}x{}", color_type, w, h);
    // 二值化
    let gray_img = img.to_luma8();
    // 高斯模糊
    let blurred_img = imageproc::filter::gaussian_blur_f32(&gray_img, 1.);
    // 膨胀
    // let dilate_img = imageproc::morphology::dilate(&blurred_img, Norm::LInf, 1);
    // // 边缘提取
    let edges_img = imageproc::edges::canny(&blurred_img, 30.0, 120.0);
    let s = Local::now().format("%Y%m%d%H%M%S").to_string();
    let p = format!("{}/{}.jpg", dir, s);
    debug!("save to {}", p);
    let _ = edges_img.save(p.to_string())?;
    Ok(p)
}

#[no_mangle]
pub extern "system" fn Java_com_example_helloso_MainActivityKt_initLog<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) {
    init_log();
}

#[no_mangle]
pub extern "system" fn Java_com_example_helloso_MainActivityKt_hello<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    dir: JString<'local>,
    input: JString<'local>,
) -> JString<'local> {
    let dir: String = env.get_string(&dir).unwrap().into();
    let input: String = env.get_string(&input).unwrap().into();
    match create_image(dir, input) {
        Ok(filepath) => env.new_string(filepath).unwrap(),
        Err(e) => {
            error!("error: {:?}", e);
            env.new_string("".to_string()).unwrap()
        }
    }
}
